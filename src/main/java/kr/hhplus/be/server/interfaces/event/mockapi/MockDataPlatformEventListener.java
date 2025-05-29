package kr.hhplus.be.server.interfaces.event.mockapi;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import kr.hhplus.be.server.interfaces.event.mockapi.payload.MockDataPlatformPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static kr.hhplus.be.server.common.constants.Topics.MOCK_API_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockDataPlatformEventListener {

    private final MessagePublisher<MockDataPlatformPayload> paymentCompletedEventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        String key = String.valueOf(event.orderId());
        MockDataPlatformPayload payload = MockDataPlatformPayload.from(event);
        paymentCompletedEventPublisher.publish(MOCK_API_TOPIC, key, payload);
    }
}

