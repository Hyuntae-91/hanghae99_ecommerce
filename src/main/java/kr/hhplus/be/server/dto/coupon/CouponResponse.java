package kr.hhplus.be.server.dto.coupon;

public record CouponResponse(
        Long couponId,
        String type,
        String description,
        Long discount,
        int state,
        String start_at,
        String end_at,
        String createdAt
) {}

