package kr.hhplus.be.server.domain.coupon.dto.request;

public record ApplyCouponDiscountServiceRequest(
        Long couponId,
        Long couponIssueId,
        Long userId,
        Long originalPrice
) {
    public ApplyCouponDiscountServiceRequest {
        if (couponId != null && couponId < 1) {
            throw new IllegalArgumentException("couponId는 1 이상이어야 합니다.");
        }
        if (couponIssueId != null && couponIssueId < 1) {
            throw new IllegalArgumentException("couponIssueId는 1 이상이어야 합니다.");
        }
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (originalPrice == null || originalPrice < 0) {
            throw new IllegalArgumentException("originalPrice는 0 이상이어야 합니다.");
        }
    }
}
