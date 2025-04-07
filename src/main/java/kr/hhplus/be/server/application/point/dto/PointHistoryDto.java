package kr.hhplus.be.server.application.point.dto;

import kr.hhplus.be.server.domain.point.model.PointHistory;

public record PointHistoryDto (
        Long userId,
        Long point,
        String type,
        String createdAt
) {
    public static PointHistoryDto from(PointHistory entity) {
        return new PointHistoryDto(
                entity.getUserId(),
                entity.getPoint(),
                entity.getType().name(),
                entity.getCreatedAt()
        );
    }
}