package kr.hhplus.be.server.interfaces.event.mockapi;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.interfaces.event.mockapi.payload.MockDataPlatformPayload;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentCompletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class MockDataPlatformEventListener {

    private final MessagePublisher<MockDataPlatformPayload> mockDataPlatformPayloadPublisher;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @KafkaListener(topics = Topics.PAYMENT_COMPLETE_TOPIC, groupId = Groups.PAYMENT_COMPLETE_MOCK_API_GROUP)
    public void handle(PaymentCompletedPayload event) {
        String key = String.valueOf(event.orderId());
        MockDataPlatformPayload payload = MockDataPlatformPayload.from(event);
        mockDataPlatformPayloadPublisher.publish(Topics.MOCK_API_TOPIC, key, payload);
    }
}

