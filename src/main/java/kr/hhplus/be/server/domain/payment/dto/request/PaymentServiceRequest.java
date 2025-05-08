package kr.hhplus.be.server.domain.payment.dto.request;

public record PaymentServiceRequest (
        Long userId,
        Long totalPrice,
        Long orderId
) {
    public PaymentServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice는 0 이상이어야 합니다.");
        }
        if (orderId == null || orderId < 0) {
            throw new IllegalArgumentException("orderId는 0 이상이어야 합니다.");
        }
    }
}
