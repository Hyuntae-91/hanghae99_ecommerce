package kr.hhplus.be.server.domain.point.dto.event;

import java.util.List;

public record PointUsedCompletedEvent(
        Long orderId,
        Long userId,
        Long usedPoint,
        List<Long> productIds
) {
    public PointUsedCompletedEvent {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (usedPoint == null || usedPoint < 0) {
            throw new IllegalArgumentException("usedPoint는 0 이상이어야 합니다.");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("productIds는 null이거나 비어 있을 수 없습니다.");
        }
    }
}
