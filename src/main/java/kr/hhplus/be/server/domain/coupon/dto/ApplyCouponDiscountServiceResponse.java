package kr.hhplus.be.server.domain.coupon.dto;

public record ApplyCouponDiscountServiceResponse(
        Long finalPrice
) {
    public ApplyCouponDiscountServiceResponse {
        if (finalPrice == null || finalPrice < 0) {
            throw new IllegalArgumentException("finalPrice는 0 이상이어야 합니다.");
        }
    }
}
