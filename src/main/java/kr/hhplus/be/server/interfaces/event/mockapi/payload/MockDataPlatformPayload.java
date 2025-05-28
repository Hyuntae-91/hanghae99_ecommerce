package kr.hhplus.be.server.interfaces.event.mockapi.payload;

import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;

import java.util.List;

public record MockDataPlatformPayload(Long orderId, Long paymentId, Long totalPrice, List<Long> productIds) {
    public static MockDataPlatformPayload from(PaymentCompletedEvent event) {
        return new MockDataPlatformPayload(
                event.orderId(),
                event.paymentId(),
                event.totalPrice(),
                event.productIds()
        );
    }
}