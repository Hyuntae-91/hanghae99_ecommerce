package kr.hhplus.be.server.domain.payment.dto.event;

import java.util.List;

public record PaymentCompletedEvent(
        Long paymentId,
        Long orderId,
        int status, // 1 = 결제 완료
        Long totalPrice,
        String createdAt,
        List<Long> productIds
) {
    public PaymentCompletedEvent {
        if (paymentId == null || paymentId < 1) {
            throw new IllegalArgumentException("paymentId는 1 이상이어야 합니다.");
        }
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice는 0 이상이어야 합니다.");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("productIds는 null 또는 비어 있을 수 없습니다.");
        }
    }
}
