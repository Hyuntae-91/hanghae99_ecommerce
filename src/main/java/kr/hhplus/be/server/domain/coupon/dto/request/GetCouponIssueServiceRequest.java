package kr.hhplus.be.server.domain.coupon.dto.request;

public record GetCouponIssueServiceRequest (
        Long couponIssueId
){
    public GetCouponIssueServiceRequest {
        if (couponIssueId == null || couponIssueId < 1) {
            throw new IllegalArgumentException("couponIssueId는 1 이상이어야 합니다.");
        }
    }
}
