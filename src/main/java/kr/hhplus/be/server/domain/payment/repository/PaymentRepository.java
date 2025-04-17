package kr.hhplus.be.server.domain.payment.repository;

import kr.hhplus.be.server.domain.payment.model.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

    Payment findById(Long id);

    void delete(Long id);
}
