package kr.hhplus.be.server.interfaces.api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductsRequest (
        @Schema(description = "페이지 번호", example = "1", defaultValue = "1")
        int page,

        @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
        int size,

        @Schema(description = "정렬 기준", example = "createdAt/name/price", defaultValue = "createdAt")
        String sort
) {}
