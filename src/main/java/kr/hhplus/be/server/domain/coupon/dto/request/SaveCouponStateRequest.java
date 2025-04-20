package kr.hhplus.be.server.domain.coupon.dto.request;

public record SaveCouponStateRequest (
        Long couponIssueId,
        int state
) {
    public SaveCouponStateRequest {
        if (couponIssueId == null || couponIssueId < 1) {
            throw new IllegalArgumentException("couponIssueId는 1 이상이어야 합니다.");
        }
        if (state < -1) {
            throw new IllegalArgumentException("state 는 -1 이상이어야 합니다.");
        }
    }
}
