package kr.hhplus.be.server.infrastructure.payment;

import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    @Override
    public Payment save(Payment payment) { return null; }

    @Override
    public Payment findById(Long id) { return null; }

    @Override
    public void delete(Long id) {}
}
