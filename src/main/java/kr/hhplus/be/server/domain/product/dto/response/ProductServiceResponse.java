package kr.hhplus.be.server.domain.product.dto.response;

import java.util.List;

public record ProductServiceResponse(
        Long id,
        String name,
        Long price,
        int state,
        String createdAt,
        List<ProductOptionResponse> options
) {
    public ProductServiceResponse {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("id는 1 이상이어야 합니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 null이거나 빈 값일 수 없습니다.");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("price는 0 이상이어야 합니다.");
        }
        if (createdAt == null || createdAt.isBlank()) {
            throw new IllegalArgumentException("createdAt은 null이거나 빈 값일 수 없습니다.");
        }
    }

    public ProductServiceResponse withOptions(List<ProductOptionResponse> newOptions) {
        return new ProductServiceResponse(
                this.id, this.name, this.price, this.state, this.createdAt, newOptions
        );
    }
}