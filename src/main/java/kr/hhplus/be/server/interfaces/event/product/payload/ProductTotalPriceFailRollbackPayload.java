package kr.hhplus.be.server.interfaces.event.product.payload;

public record ProductTotalPriceFailRollbackPayload (
        Long orderId,
        Long userId
) {
    public ProductTotalPriceFailRollbackPayload {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
    }
}
