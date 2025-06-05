package kr.hhplus.be.server.domain.coupon.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.coupon.dto.request.*;
import kr.hhplus.be.server.domain.coupon.dto.response.*;
import kr.hhplus.be.server.domain.coupon.mapper.CouponJsonMapper;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponIssuePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponRedisRepository couponRedisRepository;
    private final MessagePublisher<CouponIssuePayload> couponIssuePayloadPublisher;

    public void syncAllActiveCouponsToRedis() {
        List<Coupon> activeCoupons = couponRepository.findActiveCoupons();

        for (Coupon coupon : activeCoupons) {
            String couponJson = CouponJsonMapper.toCouponJson(coupon);
            couponRedisRepository.saveCouponInfo(coupon.getId(), couponJson);

            boolean exists = couponRedisRepository.existsStock(coupon.getId());
            if (!exists) {
                couponRedisRepository.saveStock(coupon.getId(), coupon.getQuantity());
                log.info("[CouponSyncService] 쿠폰 재고 저장 - couponId={}, quantity={}", coupon.getId(), coupon.getQuantity());
            }
        }
        log.info("[CouponSyncService] 쿠폰 정보를 모두 Redis에 저장 완료 ({}개)", activeCoupons.size());
    }

    @Cacheable(value = "getCoupon", key = "#root.args[0].userId()")
    public GetCouponsServiceResponse getCoupons(GetCouponsServiceRequest request) {
        Long userId = request.userId();

        // 1. userId 기준 발급받은 쿠폰 목록 조회 (Redis)
        Map<Long, CouponIssueRedisDto> issuedCoupons = couponRedisRepository.findAllIssuedCoupons(userId);
        if (issuedCoupons == null || issuedCoupons.isEmpty()) {
            return new GetCouponsServiceResponse(List.of());
        }

        // 2. 사용 가능한 쿠폰만 필터링
        Map<Long, Long> availableCouponMap = issuedCoupons.entrySet().stream()
                .filter(entry -> entry.getValue().getUse() == 0)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,                                        // couponId
                        entry -> entry.getValue().getCouponIssueId()
                ));

        if (availableCouponMap.isEmpty()) {
            return new GetCouponsServiceResponse(List.of());
        }

        // 3. Redis에서 쿠폰 상세 정보 다건 조회
        List<String> couponJsonList = couponRedisRepository.findCouponInfos(
                new ArrayList<>(availableCouponMap.keySet())
        );

        // 4. JSON → CouponDto 변환 + couponIssueId 세팅
        List<CouponDto> couponDtoList =
                CouponJsonMapper.fromCouponJsonList(couponJsonList, availableCouponMap);

        // 5. 결과 반환
        return new GetCouponsServiceResponse(couponDtoList);
    }

    @CircuitBreaker(
            name = "coupon_issue:" + "#root.args[0].couponId()",
            fallbackMethod = "fallbackIssueCoupon"
    )
    public IssueNewCouponServiceResponse issueNewCoupon(IssueNewCouponServiceRequest request) {
        boolean stockAvailable = couponRedisRepository.decreaseStock(request.couponId());
        if (!stockAvailable) {
            throw new IllegalArgumentException("쿠폰 소진");
        }

        // 2. 발급 기록 추가 (중복 발급 방지)
        boolean success = couponRedisRepository.addCouponForUser(
                request.userId(),
                request.couponId(),
                null,
                0
        );
        if (!success) {
            throw new IllegalArgumentException("이미 발급된 유저입니다.");
        }

        // 3. 쿠폰 정보 조회
        String couponJson = couponRedisRepository.findCouponInfo(request.couponId())
                .orElseThrow(() -> new IllegalStateException("쿠폰 정보가 존재하지 않습니다."));
        Coupon coupon = CouponJsonMapper.fromCouponJson(couponJson);

        // 4. 쿠폰 이슈 이벤트 발행
        CouponIssuePayload event = new CouponIssuePayload(
                request.userId(),
                request.couponId()
        );
        couponIssuePayloadPublisher.publish(Topics.COUPON_ISSUE_TOPIC, null, event);

        return IssueNewCouponServiceResponse.from(coupon);
    }

    @CacheEvict(value = "getCoupon", key = "#root.args[0].userId()")
    public void issueNewCouponToDb(IssueNewCouponServiceRequest request) {
        // 1. 쿠폰 상세 정보 조회
        String couponJson = couponRedisRepository.findCouponInfo(request.couponId())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 정보가 존재하지 않습니다."));
        Coupon coupon = CouponJsonMapper.fromCouponJson(couponJson);

        // 2. 쿠폰 issue 정보 redis 에 저장
        CouponIssue couponIssue = couponIssueRepository.save(CouponIssue.createNew(request.userId(), coupon, 0));
        couponRedisRepository.updateCouponIssueId(request.userId(), request.couponId(), couponIssue.getId());
    }

    @Transactional
    @CacheEvict(value = "getCoupon", key = "#root.args[0].userId()")
    public ApplyCouponDiscountServiceResponse applyCouponDiscount(ApplyCouponDiscountServiceRequest request) {
        long finalPrice = request.originalPrice();
        if (request.couponId() != null && request.couponId() > 0) {
            Long couponId = request.couponId();
            Long userId = request.userId();

            // 1. 발급 내역 전체 조회
            Map<Long, CouponIssueRedisDto> issuedCoupons = couponRedisRepository.findAllIssuedCoupons(userId);

            // 2. couponId로 발급 내역 검색
            CouponIssueRedisDto couponIssueRedis = issuedCoupons.get(couponId);
            if (couponIssueRedis == null) {
                throw new IllegalArgumentException("발급된 쿠폰이 없습니다.");
            }
            if (couponIssueRedis.getUse() == 1) {
                throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
            }

            // 3. 쿠폰 상세 정보 조회
            String couponJson = couponRedisRepository.findCouponInfo(couponId)
                    .orElseThrow(() -> new IllegalArgumentException("쿠폰 정보가 존재하지 않습니다."));

            Coupon coupon = CouponJsonMapper.fromCouponJson(couponJson);

            // 4. 최종 금액 계산
            long discountAmount = coupon.calculateDiscount(finalPrice);
            finalPrice = Math.max(0, finalPrice - discountAmount);

            // 5. 쿠폰 사용 처리 (use = 1 업데이트)
            couponRedisRepository.updateCouponUse(request.userId(), request.couponIssueId(), 1);

            // 6. CouponIssue 사용 DB 저장
            CouponIssue couponIssue = couponIssueRepository.findById(request.couponIssueId());
            couponIssue.updateState(1);
            couponIssueRepository.save(couponIssue);
        }
        return new ApplyCouponDiscountServiceResponse(finalPrice);
    }

    @Transactional
    @CacheEvict(value = "getCoupon", key = "#root.args[0].userId()")
    public void applyCouponRollback(CouponUseRequest request) {
        // 1. 쿠폰 롤백 처리
        couponRedisRepository.updateCouponUse(request.userId(), request.couponIssueId(), request.state());

        // 2. CouponIssue 사용 DB 저장
        CouponIssue couponIssue = couponIssueRepository.findById(request.couponIssueId());
        couponIssue.updateState(request.state());
        couponIssueRepository.save(couponIssue);
    }

    public IssueNewCouponServiceResponse fallbackIssueCoupon(IssueNewCouponServiceRequest request, Throwable t) {
        log.warn("Coupon issue fallback triggered for userId={}, couponId={}, reason={}",
                request.userId(), request.couponId(), t.getMessage());

        throw new IllegalArgumentException(t.getMessage());
    }
}
