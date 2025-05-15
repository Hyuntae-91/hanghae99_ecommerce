package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.dto.request.*;
import kr.hhplus.be.server.domain.coupon.dto.response.*;
import kr.hhplus.be.server.domain.coupon.mapper.CouponJsonMapper;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponRedisRepository couponRedisRepository;

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
        Map<Long, Integer> issuedCoupons = couponRedisRepository.findAllIssuedCoupons(userId);
        if (issuedCoupons == null || issuedCoupons.isEmpty()) {
            return new GetCouponsServiceResponse(List.of());
        }

        // 2. 사용 가능한 쿠폰만 필터링
        List<Long> availableCouponIds = issuedCoupons.entrySet().stream()
                .filter(entry -> Integer.parseInt(String.valueOf(entry.getValue())) == 0) // 사용 가능(0)인 쿠폰만
                .map(entry -> Long.parseLong(String.valueOf(entry.getKey())))
                .toList();

        if (availableCouponIds.isEmpty()) {
            return new GetCouponsServiceResponse(List.of());
        }

        // 3. couponId 리스트로 Redis에서 여러개 쿠폰 상세 조회
        List<String> couponJsonList = couponRedisRepository.findCouponInfos(availableCouponIds);

        // 4. JSON → CouponDto 변환
        List<CouponDto> couponDtoList = CouponJsonMapper.fromCouponJsonList(couponJsonList);

        // 5. 결과 반환
        return new GetCouponsServiceResponse(couponDtoList);
    }

    @CacheEvict(value = "getCoupon", key = "#root.args[0].userId()")
    public IssueNewCouponServiceResponse issueNewCoupon(IssueNewCouponServiceRequest request) {
        boolean stockAvailable = couponRedisRepository.decreaseStock(request.couponId());
        if (!stockAvailable) {
            throw new IllegalArgumentException("쿠폰 소진");
        }

        // 2. 발급 기록 추가 (중복 발급 방지)
        boolean success = couponRedisRepository.addCouponForUser(request.userId(), request.couponId(), 0);
        if (!success) {
            throw new IllegalArgumentException("이미 발급된 유저입니다.");
        }

        // 3. 쿠폰 정보 조회
        String couponJson = couponRedisRepository.findCouponInfo(request.couponId())
                .orElseThrow(() -> new IllegalStateException("쿠폰 정보가 존재하지 않습니다."));
        Coupon coupon = CouponJsonMapper.fromCouponJson(couponJson);

        return IssueNewCouponServiceResponse.from(coupon);
    }

    public ApplyCouponDiscountServiceResponse applyCouponDiscount(ApplyCouponDiscountServiceRequest request) {
        long finalPrice = request.originalPrice();
        if (request.couponId() != null && request.couponId() > 0) {
            Long couponId = request.couponId();
            Long userId = request.userId();

            // 1. 발급 내역 전체 조회
            Map<Long, Integer> issuedCoupons = couponRedisRepository.findAllIssuedCoupons(userId);

            // 2. couponId로 발급 내역 검색
            Integer use = issuedCoupons.get(couponId);
            if (use == null) {
                throw new IllegalArgumentException("발급된 쿠폰이 없습니다.");
            }
            if (use == 1) {
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
            couponRedisRepository.updateCouponUse(userId, couponId);

            // 6. CouponIssue 생성 후 DB 저장
            CouponIssue issue = CouponIssue.createNew(userId, coupon, 1);
            couponIssueRepository.save(issue);
        }
        return new ApplyCouponDiscountServiceResponse(finalPrice);
    }
}
