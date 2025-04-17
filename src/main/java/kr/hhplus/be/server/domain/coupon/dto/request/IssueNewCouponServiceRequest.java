package kr.hhplus.be.server.domain.coupon.dto.request;

public record IssueNewCouponServiceRequest(
        Long userId,
        Long couponId
) {
    public IssueNewCouponServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (couponId == null || couponId < 1) {
            throw new IllegalArgumentException("couponId는 1 이상이어야 합니다.");
        }
    }
}
