package kr.hhplus.be.server.application.coupon;

public record CouponIssueResponse(
        Long couponId,
        String type,
        String description,
        Long discount,
        int state,
        String start_at,
        String end_at,
        String createdAt
) {}
