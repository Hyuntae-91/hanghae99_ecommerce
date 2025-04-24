package kr.hhplus.be.server.domain.order.dto.request;

public record CreateOrderOptionDto(
        Long optionId,
        Integer quantity
) {
    public CreateOrderOptionDto {
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity는 1 이상이어야 합니다.");
        }
    }
}
