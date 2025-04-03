package kr.hhplus.be.server.dto.payment;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        int status,         // 1 = 결제 완료
        Long total_price,
        String createdAt
) {}

