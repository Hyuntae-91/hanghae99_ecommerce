package kr.hhplus.be.server.interfaces.api.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentProductDto(

        @Schema(description = "상품 ID", example = "1")
        @NotNull(message = "상품 ID는 필수입니다.")
        @Min(value = 1, message = "상품 ID는 1 이상이어야 합니다.")
        Long id,

        @Schema(description = "상품명", example = "상품상세")
        @NotNull(message = "상품명은 필수입니다.")
        String name,

        @Schema(description = "아이템 ID", example = "1")
        @NotNull(message = "아이템 ID는 필수입니다.")
        @Min(value = 1, message = "아이템 ID는 1 이상이어야 합니다.")
        Long itemId,

        @Schema(description = "옵션 ID", example = "1")
        @NotNull(message = "옵션 ID는 필수입니다.")
        @Min(value = 1, message = "옵션 ID는 1 이상이어야 합니다.")
        Long optionId,

        @Schema(description = "수량 ID", example = "1")
        @NotNull(message = "수량 ID는 필수입니다.")
        @Min(value = 1, message = "수량 ID는 1 이상이어야 합니다.")
        Integer quantity
) {
}
