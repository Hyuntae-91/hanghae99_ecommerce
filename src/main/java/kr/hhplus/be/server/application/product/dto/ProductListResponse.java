package kr.hhplus.be.server.application.product.dto;

import java.util.List;

public record ProductListResponse(
        List<ProductDto> products
) {
}
