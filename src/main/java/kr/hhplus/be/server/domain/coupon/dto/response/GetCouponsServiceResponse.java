package kr.hhplus.be.server.domain.coupon.dto.response;

import java.util.List;

public record GetCouponsServiceResponse(
        List<CouponDto> coupons
) {
    public GetCouponsServiceResponse {
        if (coupons == null || coupons.isEmpty()) {
            throw new IllegalArgumentException("보유한 쿠폰이 없습니다.");
        }
    }
}
