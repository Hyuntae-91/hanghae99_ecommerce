package kr.hhplus.be.server.interfaces.api.product.dto;


import kr.hhplus.be.server.domain.product.dto.ProductServiceResponse;

public record ProductResponse (
        Long id,
        String name,
        Long price,
        int state,
        String createdAt
) {
    public static ProductResponse from(ProductServiceResponse dto) {
        return new ProductResponse(dto.id(), dto.name(), dto.price(), dto.state(), dto.createdAt());
    }
}
