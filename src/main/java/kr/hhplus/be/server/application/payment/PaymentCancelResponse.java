package kr.hhplus.be.server.application.payment;

public record PaymentCancelResponse(
        Long orderId,
        int status
) {}
