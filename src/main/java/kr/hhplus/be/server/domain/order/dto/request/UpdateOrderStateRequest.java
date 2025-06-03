package kr.hhplus.be.server.domain.order.dto.request;

public record UpdateOrderStateRequest (
        Long orderId,
        Integer state
) {
    public UpdateOrderStateRequest {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
    }
}
