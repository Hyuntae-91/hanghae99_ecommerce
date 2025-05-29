package kr.hhplus.be.server.config;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.infrastructure.messaging.KafkaProducer;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponIssuePayload;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponUsePayload;
import kr.hhplus.be.server.interfaces.event.mockapi.payload.MockDataPlatformPayload;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class MessagingConfig {

    @Bean
    public MessagePublisher<MockDataPlatformPayload> paymentCompletedEventPublisher(
            KafkaTemplate<String, MockDataPlatformPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<CouponIssuePayload> couponIssueEventPublisher(
            KafkaTemplate<String, CouponIssuePayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<CouponUsePayload> couponUsePayloadPublisher(
            KafkaTemplate<String, CouponUsePayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }
}
