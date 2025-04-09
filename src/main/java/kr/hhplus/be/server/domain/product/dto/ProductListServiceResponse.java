package kr.hhplus.be.server.domain.product.dto;

import java.util.List;

public record ProductListServiceResponse(
        List<ProductServiceResponse> products
) {
    public ProductListServiceResponse {
        if (products == null) {
            throw new IllegalArgumentException("products는 null일 수 없습니다.");
        }
    }
}
