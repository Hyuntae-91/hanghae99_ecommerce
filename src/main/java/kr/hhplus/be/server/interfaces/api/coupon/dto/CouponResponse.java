package kr.hhplus.be.server.interfaces.api.coupon.dto;

public record CouponResponse(
        Long couponId,
        String type,
        String description,
        Integer discount,
        int state,
        String start_at,
        String end_at,
        String createdAt
) {}

