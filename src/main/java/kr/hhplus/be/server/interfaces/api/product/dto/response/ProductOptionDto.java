package kr.hhplus.be.server.interfaces.api.product.dto.response;

import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;

public record ProductOptionDto (
        Long optionId,
        int size,
        int stock
) {
    public ProductOptionDto {
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }

        if (size < 0) {
            throw new IllegalArgumentException("size는 0 이상이어야 합니다.");
        }

        if (stock < 0) {
            throw new IllegalArgumentException("stock은 0 이상이어야 합니다.");
        }
    }

    public static ProductOptionDto from(ProductOptionResponse option) {
        return new ProductOptionDto(
                option.optionId(),
                option.size(),
                option.stock()
        );
    }
}
