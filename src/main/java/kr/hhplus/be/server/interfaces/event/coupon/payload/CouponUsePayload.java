package kr.hhplus.be.server.interfaces.event.coupon.payload;

public record CouponUsePayload(Long userId, Long couponIssueId) {
    public CouponUsePayload {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (couponIssueId != null && couponIssueId < 1) {
            throw new IllegalArgumentException("couponIssueId는 1 이상이거나 null이어야 합니다.");
        }
    }
}
