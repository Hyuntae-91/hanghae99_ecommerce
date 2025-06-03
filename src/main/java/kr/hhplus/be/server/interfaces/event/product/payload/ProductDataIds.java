package kr.hhplus.be.server.interfaces.event.product.payload;

public record ProductDataIds(
        Long productId,
        Long optionId,
        Long itemId,
        Integer quantity
) {
    public ProductDataIds {
        if (productId == null || productId < 1) {
            throw new IllegalArgumentException("productId는 1 이상이어야 합니다.");
        }
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }
        if (itemId == null || itemId < 1) {
            throw new IllegalArgumentException("itemId는 1 이상이어야 합니다.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity는 1 이상이어야 합니다.");
        }
    }
}
