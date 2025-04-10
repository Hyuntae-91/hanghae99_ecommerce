package kr.hhplus.be.server.infrastructure.product.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record GetProductsRepositoryRequestDto (
        int page,
        int size,
        String sort
) {
    public Pageable getPageable() {
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort()));
    }
}
