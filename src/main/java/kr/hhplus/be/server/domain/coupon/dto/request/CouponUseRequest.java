package kr.hhplus.be.server.domain.coupon.dto.request;

public record CouponUseRequest (
    Long userId,
    Long couponIssueId,
    int state
) {
    public CouponUseRequest {
        if (couponIssueId != null && couponIssueId < 1) {
            throw new IllegalArgumentException("couponIssueId는 1 이상이어야 합니다.");
        }
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
    }
}
