package kr.hhplus.be.server.domain.product.dto.request;

import jakarta.validation.constraints.Min;

public record BestProductRequest(
        @Min(0)
        int page,

        @Min(1)
        int size
) {
}