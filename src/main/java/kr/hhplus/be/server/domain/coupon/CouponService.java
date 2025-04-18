package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.dto.*;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

    public CouponIssueDto getCouponIssueById(GetCouponIssueServiceRequest request) {
        CouponIssue couponIssue = couponIssueRepository.findById(request.couponIssueId());
        return CouponIssueDto.from(couponIssue);
    }

    public GetCouponsServiceResponse getCoupons(GetCouponsServiceRequest request) {
        Long userId = request.userId();

        // 1. 사용자 보유 쿠폰 조회
        List<CouponIssue> couponIssues = couponIssueRepository.findByUserId(userId);

        // 2. 쿠폰 정보를 CouponDto로 매핑
        List<CouponIssueDto> couponDtoList = couponIssues.stream()
                .map(CouponIssue::toDto)
                .toList();

        return new GetCouponsServiceResponse(couponDtoList);
    }

    public IssueNewCouponServiceResponse issueNewCoupon(IssueNewCouponServiceRequest request) {
        Coupon coupon = couponRepository.findById(request.couponId());
        coupon.validateIssuable();

        String now = java.time.LocalDateTime.now().toString();
        String end = java.time.LocalDateTime.now()
                .plusDays(coupon.getExpirationDays())
                .toString();

        CouponIssue issue = CouponIssue.builder()
                .userId(request.userId())
                .coupon(coupon)
                .state(0) // 0: 사용 가능
                .startAt(now)
                .endAt(end)
                .createdAt(now)
                .updatedAt(now)
                .build();

        couponIssueRepository.save(issue);

        // 발급 수량 증가
        coupon.increaseIssued();
        couponRepository.save(coupon);
        return IssueNewCouponServiceResponse.from(issue);
    }

    public void saveState(SaveCouponStateRequest request) {
        CouponIssue couponIssue = couponIssueRepository.findById(request.couponIssueId());

        couponIssue.updateState(request.state());
        couponIssueRepository.save(couponIssue);
    }

    public ApplyCouponDiscountServiceResponse applyCouponDiscount(ApplyCouponDiscountServiceRequest request) {
        CouponIssue issue = couponIssueRepository.findById(request.couponIssueId());
        issue.validateUsable();
        long finalPrice = issue.calculateFinalPrice(request.originalPrice());
        return new ApplyCouponDiscountServiceResponse(finalPrice);
    }
}
