package kr.hhplus.be.server.interfaces.api.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PaymentRequest(

        @Schema(description = "주문 상품 리스트")
        @NotNull(message = "상품 목록은 필수입니다.")
        @Valid
        List<PaymentProductDto> products,

        @Schema(description = "사용할 쿠폰 ID", example = "1", nullable = true)
        Long couponId

) {
}
