package kr.hhplus.be.server.interfaces.api.payment.dto;

public record PaymentCancelResponse(
        Long orderId,
        int status
) {}
