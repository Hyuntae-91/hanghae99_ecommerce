package kr.hhplus.be.server.domain.product.dto.request;

public record ProductListServiceRequest(
        Long cursorId,
        int size,
        String sort
) {
    public ProductListServiceRequest {
        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one");
        }
        if (sort == null || sort.isBlank()) {
            throw new IllegalArgumentException("sort 필드는 null이거나 빈 값이 될 수 없습니다.");
        }
    }
}