package kr.hhplus.be.server.domain.order.dto;

import java.util.List;

public record CreateOrderServiceRequest(
        Long userId,
        Long couponIssueId,
        List<CreateOrderItemDto> items
) {
    public CreateOrderServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items는 비어 있을 수 없습니다.");
        }
    }
}
