package kr.hhplus.be.server.domain.point.dto;

public record PointChargeResponseDto (
        Long point
) {
    public PointChargeResponseDto {
        if (point == null || point < 0) {
            throw new IllegalArgumentException("point는 0 이상이어야 합니다.");
        }
    }
}
