package kr.hhplus.be.server.domain.product.dto;

public record ProductServiceRequest(Long productId) {
    public ProductServiceRequest {
        if (productId == null || productId < 1) {
            throw new IllegalArgumentException("productId는 1 이상이어야 합니다.");
        }
    }
}