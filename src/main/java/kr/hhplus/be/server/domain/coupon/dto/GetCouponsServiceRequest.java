package kr.hhplus.be.server.domain.coupon.dto;

public record GetCouponsServiceRequest(
        Long userId
) {
    public GetCouponsServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
    }
}
