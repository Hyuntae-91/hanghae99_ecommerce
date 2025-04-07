package kr.hhplus.be.server.application.product.dto;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        Long price,
        int state,
        String createdAt
) {
}
