package kr.hhplus.be.server.interfaces.api.order.dto;

import kr.hhplus.be.server.domain.order.dto.CartItemResponse;

public record CartItem(
        Long productId,
        int quantity,
        Long optionId,
        Long eachPrice,
        int stockQuantity,
        int size
) {
    public CartItem {
        if (productId == null || productId < 1) {
            throw new IllegalArgumentException("productId는 1 이상이어야 합니다.");
        }
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity는 1 이상이어야 합니다.");
        }
        if (eachPrice == null || eachPrice < 0) {
            throw new IllegalArgumentException("eachPrice는 0 이상이어야 합니다.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("stockQuantity는 0 이상이어야 합니다.");
        }
        if (size < 0) {
            throw new IllegalArgumentException("size는 0 이상이어야 합니다.");
        }
    }

    public static CartItem from(CartItemResponse cartItemResponse) {
        return new CartItem(
                cartItemResponse.productId(),
                cartItemResponse.quantity(),
                cartItemResponse.optionId(),
                cartItemResponse.eachPrice(),
                cartItemResponse.stockQuantity(),
                cartItemResponse.size()
        );
    }
}
