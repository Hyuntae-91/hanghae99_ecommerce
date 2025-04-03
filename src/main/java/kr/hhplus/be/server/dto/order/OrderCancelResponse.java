package kr.hhplus.be.server.dto.order;

public record OrderCancelResponse(
        Long orderId,
        int status
) {}
