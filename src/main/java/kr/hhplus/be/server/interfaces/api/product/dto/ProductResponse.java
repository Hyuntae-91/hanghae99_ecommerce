package kr.hhplus.be.server.interfaces.api.product.dto;

import kr.hhplus.be.server.domain.product.dto.ProductServiceResponse;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        Long optionId,
        int size,
        int stock,
        Long price,
        int state,
        String createdAt
) {
    public static List<ProductResponse> from(ProductServiceResponse dto) {
        return dto.options().stream()
                .map(opt -> new ProductResponse(
                        dto.id(),
                        dto.name(),
                        opt.optionId(),
                        opt.size(),
                        opt.stock(),
                        dto.price(),
                        dto.state(),
                        dto.createdAt()
                ))
                .toList();
    }
}
