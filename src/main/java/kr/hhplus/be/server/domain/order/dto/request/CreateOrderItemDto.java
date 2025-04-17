package kr.hhplus.be.server.domain.order.dto.request;

public record CreateOrderItemDto(
        Long itemId,
        Integer quantity
) {
    public CreateOrderItemDto {
        if (itemId == null || itemId < 1) {
            throw new IllegalArgumentException("itemId는 1 이상이어야 합니다.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity는 1 이상이어야 합니다.");
        }
    }
}
