package kr.hhplus.be.server.domain.coupon.dto;

import kr.hhplus.be.server.domain.coupon.model.CouponIssue;

public record IssueNewCouponServiceResponse(
        Long couponId,
        String type,         // PERCENT or FIXED
        String description,
        Integer discount,
        Integer state,       // 0: 사용 가능
        String start_at,
        String end_at,
        String createdAt
) {
    public static IssueNewCouponServiceResponse from(CouponIssue issue) {
        var coupon = issue.getCoupon();
        return new IssueNewCouponServiceResponse(
                coupon.getId(),
                coupon.getType().name(),
                coupon.getDescription(),
                coupon.getDiscount(),
                issue.getState(),
                issue.getStartAt(),
                issue.getEndAt(),
                issue.getCreatedAt()
        );
    }
}
