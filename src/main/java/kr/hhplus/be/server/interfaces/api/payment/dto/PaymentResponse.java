package kr.hhplus.be.server.interfaces.api.payment.dto;

import kr.hhplus.be.server.domain.payment.dto.PaymentServiceResponse;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        int status,         // 1 = 결제 완료
        Long total_price,
        String createdAt
) {
    public static PaymentResponse from(PaymentServiceResponse svcDto) {
        return new PaymentResponse(
                svcDto.paymentId(),
                svcDto.orderId(),
                svcDto.status(),
                svcDto.totalPrice(),
                svcDto.createdAt()
        );
    }
}

