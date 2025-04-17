package kr.hhplus.be.server.domain.order.dto.request;

public record AddCartServiceRequest(
        Long userId,
        Long productId,
        Long optionId,
        Long eachPrice,
        Integer quantity
) {
    public AddCartServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (productId == null || productId < 1) {
            throw new IllegalArgumentException("productId는 1 이상이어야 합니다.");
        }
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }
        if (eachPrice == null || eachPrice < 0) {
            throw new IllegalArgumentException("eachPrice는 0 이상이어야 합니다.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity는 1 이상이어야 합니다.");
        }
    }
}
