package kr.hhplus.be.server.domain.point.dto;

public record PointChargeServiceResponse(
        Long point
) {
    public PointChargeServiceResponse {
        if (point == null || point < 0) {
            throw new IllegalArgumentException("point는 0 이상이어야 합니다.");
        }
    }
}
