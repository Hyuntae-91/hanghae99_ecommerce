package kr.hhplus.be.server.domain.order.dto.response;

public record CreateOrderServiceResponse(
        Long orderId
) {
    public CreateOrderServiceResponse {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
    }
}
