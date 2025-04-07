package kr.hhplus.be.server.application.point.dto;

public record PointResponse(
        Long userId,
        Long point
) {
    public static PointResponse from(UserPointDto dto) {
        return new PointResponse(dto.userId(), dto.point());
    }
}