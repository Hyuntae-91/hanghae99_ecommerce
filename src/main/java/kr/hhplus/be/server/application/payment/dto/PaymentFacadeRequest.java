package kr.hhplus.be.server.application.payment.dto;

import java.util.List;

public record PaymentFacadeRequest (
        Long userId,
        List<PaymentProductFacadeDto> products,
        Long couponIssueId
) {}
