package kr.hhplus.be.server.interfaces.api.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PointHistoryRequest (

        @Schema(description = "페이지 번호", example = "1", defaultValue = "1")
        @Min(value = 1, message = "page는 1 이상이어야 합니다.")
        int page,

        @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
        @Min(value = 1, message = "size는 1 이상이어야 합니다.")
        int size,

        @Schema(description = "정렬 기준", example = "createdAt", defaultValue = "createdAt")
        @NotBlank(message = "sort는 null이거나 빈 값이 될 수 없습니다.")
        String sort

) {}
