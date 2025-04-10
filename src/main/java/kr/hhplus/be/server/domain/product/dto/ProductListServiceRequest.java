package kr.hhplus.be.server.domain.product.dto;

public record ProductListServiceRequest(
        int page,
        int size,
        String sort
) {
    public ProductListServiceRequest {
        if (page < 1) {
            throw new IllegalArgumentException("Page index must not be less than one");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one");
        }
        if (sort == null || sort.isBlank()) {
            throw new IllegalArgumentException("sort 필드는 null이거나 빈 값이 될 수 없습니다.");
        }
    }
}