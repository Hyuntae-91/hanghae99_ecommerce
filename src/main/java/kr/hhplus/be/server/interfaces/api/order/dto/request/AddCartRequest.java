package kr.hhplus.be.server.interfaces.api.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartRequest(

        @Schema(description = "상품 ID", example = "1")
        @NotNull(message = "productId는 필수입니다.")
        @Min(value = 1, message = "productId는 1 이상이어야 합니다.")
        Long productId,

        @Schema(description = "옵션 ID", example = "1")
        @NotNull(message = "optionId는 필수입니다.")
        @Min(value = 1, message = "optionId는 1 이상이어야 합니다.")
        Long optionId,

        @Schema(description = "상품 수량", example = "2")
        @Min(value = 1, message = "quantity는 1 이상이어야 합니다.")
        int quantity
) {}
