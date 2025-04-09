package kr.hhplus.be.server.interfaces.api.product.dto;

import kr.hhplus.be.server.domain.product.dto.ProductListServiceResponse;
import kr.hhplus.be.server.domain.product.dto.ProductServiceResponse;

import java.util.List;

public record ProductListResponse(
        List<ProductResponse> products
) {
    public static ProductListResponse from(ProductListServiceResponse serviceResponses) {
        if (serviceResponses == null) {
            throw new IllegalArgumentException("serviceResponses는 null일 수 없습니다.");
        }

        List<ProductResponse> mapped = serviceResponses.products().stream()
                .map(ProductResponse::from)
                .toList();

        return new ProductListResponse(mapped);
    }
}