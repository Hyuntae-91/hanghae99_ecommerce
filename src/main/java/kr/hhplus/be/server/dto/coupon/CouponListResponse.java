package kr.hhplus.be.server.dto.coupon;

import java.util.List;

public record CouponListResponse(
        List<CouponResponse> coupons
) {}
