package kr.hhplus.be.server.domain.point.dto.request;

public record PointChargeServiceRequest(
        Long userId,
        Long point
) {
    public PointChargeServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (point == null || point <= 0) {
            throw new IllegalArgumentException("충전 포인트는 0보다 커야 합니다.");
        }
    }
}

