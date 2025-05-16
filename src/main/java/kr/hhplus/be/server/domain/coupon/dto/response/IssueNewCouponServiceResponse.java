package kr.hhplus.be.server.domain.coupon.dto.response;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;

public record IssueNewCouponServiceResponse(
        Long couponId,
        String type,         // PERCENT or FIXED
        String description,
        Integer discount
) {
    public static IssueNewCouponServiceResponse from(Coupon coupon) {
        return new IssueNewCouponServiceResponse(
                coupon.getId(),
                coupon.getType().name(),
                coupon.getDescription(),
                coupon.getDiscount()
        );
    }
}
