package kr.hhplus.be.server.application.payment.dto;

public record PaymentProductFacadeDto(
        Long productId,
        String name,
        Long optionId,
        Long itemId,
        Integer quantity
) {}