package kr.hhplus.be.server.application.payment;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        int status,         // 1 = 결제 완료
        Long total_price,
        String createdAt
) {}

