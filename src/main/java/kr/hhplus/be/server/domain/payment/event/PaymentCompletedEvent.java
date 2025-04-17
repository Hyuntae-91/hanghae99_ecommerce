package kr.hhplus.be.server.domain.payment.event;

import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;

public record PaymentCompletedEvent(PaymentServiceResponse payment) {}