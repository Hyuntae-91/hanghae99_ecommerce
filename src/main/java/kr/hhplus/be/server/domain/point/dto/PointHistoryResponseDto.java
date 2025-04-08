package kr.hhplus.be.server.domain.point.dto;

import kr.hhplus.be.server.domain.point.model.PointHistory;

public record PointHistoryResponseDto(
        Long userId,
        Long point,
        String type,
        String createdAt
) {
    public PointHistoryResponseDto {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (point == null || point < 0) {
            throw new IllegalArgumentException("point는 0 이상이어야 합니다.");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type은 null이거나 빈 값이 될 수 없습니다.");
        }
        if (createdAt == null || createdAt.isBlank()) {
            throw new IllegalArgumentException("createdAt은 null이거나 빈 값이 될 수 없습니다.");
        }
    }

    public static PointHistoryResponseDto from(PointHistory entity) {
        return new PointHistoryResponseDto(
                entity.getUserId(),
                entity.getPoint(),
                entity.getType().name(),
                entity.getCreatedAt()
        );
    }
}