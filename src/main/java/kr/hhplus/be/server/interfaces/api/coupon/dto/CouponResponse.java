package kr.hhplus.be.server.interfaces.api.coupon.dto;

public record CouponResponse(
        Long id,
        Long issueId,
        int discount,
        String description,
        int expirationDays
) {}