package kr.hhplus.be.server.domain.product.dto;

public record ProductOptionKeyDto(
        Long productId,
        Long optionId
) {
    public ProductOptionKeyDto {
        if (productId == null || productId < 1) {
            throw new IllegalArgumentException("productId는 1 이상이어야 합니다.");
        }
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }
    }
}
