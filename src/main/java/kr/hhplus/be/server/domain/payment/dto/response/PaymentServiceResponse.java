package kr.hhplus.be.server.domain.payment.dto.response;

public record PaymentServiceResponse (
        Long paymentId,
        Long orderId,
        int status,         // 1 = 결제 완료
        Long totalPrice,
        String createdAt
) {}
