package kr.hhplus.be.server.interfaces.api.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PointChargeRequest(
        @Schema(description = "충전 포인트", example = "100")
        @NotNull(message = "포인트 값은 필수입니다.")
        @Min(value = 1, message = "충전 금액은 0보다 커야 합니다.")
        Long point
) {}
