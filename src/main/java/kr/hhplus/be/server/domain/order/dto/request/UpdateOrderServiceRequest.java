package kr.hhplus.be.server.domain.order.dto.request;

public record UpdateOrderServiceRequest(
        Long orderId,
        Long totalPrice
) {
    public UpdateOrderServiceRequest {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice는 0 이상이어야 합니다.");
        }
    }
}
