package kr.hhplus.be.server.domain.payment.event;

import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.mapper.PaymentMapper;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.point.dto.event.PointUsedCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentCompleted(PointUsedCompletedEvent event) {

        PaymentServiceResponse response = paymentService.pay(paymentMapper.toPaymentServiceRequest(event));
        publisher.publishEvent(
                paymentMapper.toPaymentCompletedEvent(response, event.productIds())
        );
    }
}
