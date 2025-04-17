package kr.hhplus.be.server.interfaces.api.coupon.dto;

import kr.hhplus.be.server.domain.coupon.dto.response.GetCouponsServiceResponse;

import java.util.List;

public record CouponListResponse(
        List<CouponResponse> coupons
) {
    public static CouponListResponse from(GetCouponsServiceResponse serviceResponse) {
        List<CouponResponse> responses = serviceResponse.coupons().stream()
                .map(coupon -> new CouponResponse(
                        coupon.couponId(),
                        coupon.type(),
                        coupon.description(),
                        coupon.discount(),
                        coupon.state(),
                        coupon.startAt(),
                        coupon.endAt(),
                        coupon.createdAt()
                ))
                .toList();

        return new CouponListResponse(responses);
    }
}
