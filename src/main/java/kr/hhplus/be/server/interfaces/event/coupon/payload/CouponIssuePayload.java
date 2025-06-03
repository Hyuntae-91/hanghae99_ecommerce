package kr.hhplus.be.server.interfaces.event.coupon.payload;

public record CouponIssuePayload(Long userId, Long couponId) {
    public CouponIssuePayload {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (couponId != null && couponId < 1) {
            throw new IllegalArgumentException("couponId는 1 이상이거나 null이어야 합니다.");
        }
    }
}
