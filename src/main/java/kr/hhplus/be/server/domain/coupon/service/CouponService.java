package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.dto.request.*;
import kr.hhplus.be.server.domain.coupon.dto.response.ApplyCouponDiscountServiceResponse;
import kr.hhplus.be.server.domain.coupon.dto.response.CouponIssueDto;
import kr.hhplus.be.server.domain.coupon.dto.response.GetCouponsServiceResponse;
import kr.hhplus.be.server.domain.coupon.dto.response.IssueNewCouponServiceResponse;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
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
        Coupon coupon = couponRepository.findById(couponIssue.getCouponId());
        return CouponIssueDto.from(couponIssue, coupon);
    }

    public GetCouponsServiceResponse getCoupons(GetCouponsServiceRequest request) {
        Long userId = request.userId();

        // 1. 사용자 보유 쿠폰 조회
        List<CouponIssue> couponIssues = couponIssueRepository.findUsableByUserId(userId);

        // 2. 쿠폰 정보를 CouponDto로 매핑
        List<CouponIssueDto> couponDtoList = couponIssues.stream()
                .map(issue -> {
                    Coupon coupon = couponRepository.findById(issue.getCouponId());
                    return issue.toDto(coupon);
                })
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
                .couponId(coupon.getId())
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
        return IssueNewCouponServiceResponse.from(issue, coupon);
    }

    public void saveState(SaveCouponStateRequest request) {
        CouponIssue couponIssue = couponIssueRepository.findById(request.couponIssueId());

        couponIssue.updateState(request.state());
        couponIssueRepository.save(couponIssue);
    }

    public ApplyCouponDiscountServiceResponse applyCouponDiscount(ApplyCouponDiscountServiceRequest request) {
        CouponIssue issue = couponIssueRepository.findById(request.couponIssueId());
        issue.validateUsable();

        Coupon coupon = couponRepository.findById(issue.getCouponId());
        long finalPrice = issue.calculateFinalPrice(request.originalPrice(), coupon);
        return new ApplyCouponDiscountServiceResponse(finalPrice);
    }
}
