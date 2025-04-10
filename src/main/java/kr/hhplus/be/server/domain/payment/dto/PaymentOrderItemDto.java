package kr.hhplus.be.server.domain.payment.dto;


public record PaymentOrderItemDto(
        Long orderItemId,
        Long optionId
) {
    public PaymentOrderItemDto {
        if (orderItemId == null || orderItemId < 1) {
            throw new IllegalArgumentException("orderItemId는 1 이상이어야 합니다.");
        }
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }
    }
}
