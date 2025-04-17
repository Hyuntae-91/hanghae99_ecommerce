package kr.hhplus.be.server.interfaces.api.payment.dto.response;

public record PaymentCancelResponse(
        Long orderId,
        int status
) {}
