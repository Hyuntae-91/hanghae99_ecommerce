package kr.hhplus.be.server.dto.order;

public record OrderResponse(
        Long orderId,
        int status,
        Long total_price,
        int quantity,
        Long coupon_issue_id,
        String createdAt
) {}
