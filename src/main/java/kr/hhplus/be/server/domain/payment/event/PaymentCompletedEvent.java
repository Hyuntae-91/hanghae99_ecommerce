package kr.hhplus.be.server.domain.payment.event;

import kr.hhplus.be.server.domain.payment.dto.PaymentServiceResponse;

public record PaymentCompletedEvent(PaymentServiceResponse payment) {}