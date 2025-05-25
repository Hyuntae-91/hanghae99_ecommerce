package kr.hhplus.be.server.interfaces.api.payment.dto.response;

import kr.hhplus.be.server.domain.order.dto.response.CreateOrderServiceResponse;

public record PaymentResponse(
        Long orderId
) {
    public static PaymentResponse from(CreateOrderServiceResponse svcDto) {
        return new PaymentResponse(
                svcDto.orderId()
        );
    }
}

