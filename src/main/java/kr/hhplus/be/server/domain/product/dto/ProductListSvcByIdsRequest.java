package kr.hhplus.be.server.domain.product.dto;

import java.util.List;

public record ProductListSvcByIdsRequest(
        List<ProductOptionKeyDto> items
) {
    public ProductListSvcByIdsRequest {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("상품 목록은 비어 있을 수 없습니다.");
        }
    }
}
