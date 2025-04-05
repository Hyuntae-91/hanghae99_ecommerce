package kr.hhplus.be.server.dto.product;

import java.util.List;

public record ProductListResponse(
        List<ProductResponse> products
) {}