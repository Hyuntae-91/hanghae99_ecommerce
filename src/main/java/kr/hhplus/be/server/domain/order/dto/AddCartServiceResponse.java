package kr.hhplus.be.server.domain.order.dto;

import java.util.List;

public record AddCartServiceResponse(
        List<CartItemResponse> cartList,
        Long totalPrice
) {
    public AddCartServiceResponse {
        if (cartList == null || cartList.isEmpty()) {
            throw new IllegalArgumentException("cartList는 null이거나 비어 있을 수 없습니다.");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice는 0 이상이어야 합니다.");
        }
    }
}
