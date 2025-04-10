package kr.hhplus.be.server.domain.product.dto;

import java.util.List;

public record ProductListServiceDto(
        List<ProductServiceResponse> products
) {
    public ProductListServiceDto {
        if (products == null) {
            throw new IllegalArgumentException("products는 null일 수 없습니다.");
        }
    }
}
