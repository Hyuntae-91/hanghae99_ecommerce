package kr.hhplus.be.server.interfaces.api.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PointChargeRequest(
        @Schema(description = "충전 포인트", example = "100")
        Long point
) {
    public void validate() {
        if (point == null || point <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
    }
}
