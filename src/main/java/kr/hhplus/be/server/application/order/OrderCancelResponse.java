package kr.hhplus.be.server.application.order;

public record OrderCancelResponse(
        Long orderId,
        int status
) {}
