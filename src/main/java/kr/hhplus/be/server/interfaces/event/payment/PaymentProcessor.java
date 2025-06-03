package kr.hhplus.be.server.interfaces.event.payment;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.mapper.PaymentMapper;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentCompletedPayload;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentRollbackPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUsedCompletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final MessagePublisher<PaymentCompletedPayload> paymentCompletedPayloadPublisher;
    private final MessagePublisher<PaymentRollbackPayload> paymentRollbackPayloadPublisher;

    @KafkaListener(topics = Topics.USE_USER_POINT_COMPLETE_TOPIC, groupId = Groups.USE_USER_POINT_COMPLETE_GROUP)
    public void consume(@Payload PointUsedCompletedPayload event) {
        try {
            PaymentServiceResponse response = paymentService.pay(paymentMapper.toPaymentServiceRequest(event));
            paymentCompletedPayloadPublisher.publish(
                    Topics.PAYMENT_COMPLETE_TOPIC,
                    null,
                    paymentMapper.toPaymentCompletedPayload(response, event)
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            paymentRollbackPayloadPublisher.publish(
                    Topics.PAYMENT_FAILED_TOPIC,
                    null,
                    paymentMapper.toPaymentRollbackPayload(event)
            );
        }
    }
}
