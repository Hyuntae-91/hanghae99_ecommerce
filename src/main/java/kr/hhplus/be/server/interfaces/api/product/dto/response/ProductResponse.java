package kr.hhplus.be.server.interfaces.api.product.dto.response;

import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        Long price,
        int state,
        String createdAt,
        List<ProductOptionDto> options
) {
    public static ProductResponse from(ProductServiceResponse dto) {
        List<ProductOptionDto> options = dto.options().stream()
                .map(ProductOptionDto::from)
                .toList();

        return new ProductResponse(
                dto.id(),
                dto.name(),
                dto.price(),
                dto.state(),
                dto.createdAt(),
                options
        );
    }

    public static List<ProductResponse> fromList(List<ProductServiceResponse> serviceList) {
        return serviceList.stream()
                .map(ProductResponse::from)
                .toList();
    }
}
