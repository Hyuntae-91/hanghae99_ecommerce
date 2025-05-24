package kr.hhplus.be.server.domain.point.dto.event;

import java.util.List;

public record PointUsedCompletedEvent(
        Long orderId,
        Long userId,
        Long usedPoint,
        List<Long> productIds
) {
    public PointUsedCompletedEvent {
        if (orderId == null || orderId < 1) throw new IllegalArgumentException("orderId 필수");
        if (userId == null || userId < 1) throw new IllegalArgumentException("userId 필수");
        if (usedPoint == null || usedPoint < 0) throw new IllegalArgumentException("usedPoint 필수");
        if (productIds == null || productIds.isEmpty()) throw new IllegalArgumentException("productIds 비어있음");
    }
}
