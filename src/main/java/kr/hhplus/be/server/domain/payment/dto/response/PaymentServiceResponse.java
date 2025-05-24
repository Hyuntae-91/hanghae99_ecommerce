package kr.hhplus.be.server.domain.payment.dto.response;

public record PaymentServiceResponse (
        Long paymentId,
        Long orderId,
        int status,         // 1 = 결제 완료
        Long totalPrice,
        String createdAt
) {
    public PaymentServiceResponse {
        if (paymentId == null || paymentId < 1) {
            throw new IllegalArgumentException("paymentId는 1 이상이어야 합니다.");
        }
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice는 0 이상이어야 합니다.");
        }
        if (createdAt == null || createdAt.isBlank()) {
            throw new IllegalArgumentException("createdAt은 null이거나 공백일 수 없습니다.");
        }
    }
}
