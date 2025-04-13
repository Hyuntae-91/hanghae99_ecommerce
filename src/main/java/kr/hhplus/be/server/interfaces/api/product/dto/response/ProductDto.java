package kr.hhplus.be.server.interfaces.api.product.dto.response;

import kr.hhplus.be.server.domain.product.model.Product;
import lombok.Builder;


@Builder
public record ProductDto(
        Long id,
        String name,
        Long price,
        int state,
        String createdAt
) {

    public static ProductDto from(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .state(product.getState())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
