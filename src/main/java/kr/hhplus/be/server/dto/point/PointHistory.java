package kr.hhplus.be.server.dto.point;

public record PointHistory(
        Long userId,
        Long point,
        String type,
        String createdAt
) {}