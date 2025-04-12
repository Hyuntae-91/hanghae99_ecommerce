package kr.hhplus.be.server.domain.point.dto.response;

public record PointChargeServiceResponse(
        Long userId,
        Long point
) {
    public PointChargeServiceResponse {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (point == null || point < 0) {
            throw new IllegalArgumentException("point는 0 이상이어야 합니다.");
        }
    }
}
