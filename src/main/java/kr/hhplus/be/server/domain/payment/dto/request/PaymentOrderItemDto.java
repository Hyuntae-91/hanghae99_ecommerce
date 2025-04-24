package kr.hhplus.be.server.domain.payment.dto.request;


public record PaymentOrderItemDto(
        Long orderId,
        Long orderItemId,
        Long optionId,
        Integer quantity
) {
    public PaymentOrderItemDto {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
        if (orderItemId == null || orderItemId < 1) {
            throw new IllegalArgumentException("orderItemId는 1 이상이어야 합니다.");
        }
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity는 1 이상이어야 합니다.");
        }
    }
}
