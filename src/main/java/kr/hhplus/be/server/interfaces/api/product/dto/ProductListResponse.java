package kr.hhplus.be.server.interfaces.api.product.dto;

import kr.hhplus.be.server.domain.product.dto.ProductListServiceDto;

import java.util.List;

public record ProductListResponse(
        List<ProductResponse> products
) {
    public static ProductListResponse from(ProductListServiceDto serviceResponses) {
        if (serviceResponses == null) {
            throw new IllegalArgumentException("serviceResponses는 null일 수 없습니다.");
        }

        List<ProductResponse> mapped = serviceResponses.products().stream()
                .flatMap(dto -> ProductResponse.from(dto).stream())
                .toList();

        return new ProductListResponse(mapped);
    }
}
