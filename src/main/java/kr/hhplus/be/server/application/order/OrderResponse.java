package kr.hhplus.be.server.application.order;

public record OrderResponse(
        Long orderId,
        int status,
        Long total_price,
        int quantity,
        Long coupon_issue_id,
        String createdAt
) {}
