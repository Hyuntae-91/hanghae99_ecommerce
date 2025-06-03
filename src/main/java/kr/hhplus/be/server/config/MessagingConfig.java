package kr.hhplus.be.server.config;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.infrastructure.messaging.KafkaProducer;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponUseRollbackPayload;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponApplyCompletedPayload;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponIssuePayload;
import kr.hhplus.be.server.interfaces.event.mockapi.payload.MockDataPlatformPayload;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentCompletedPayload;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentRollbackPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUseRollbackPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUsedCompletedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.OrderCreatedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductTotalPriceCompletedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductTotalPriceFailRollbackPayload;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class MessagingConfig {

    @Bean
    public MessagePublisher<MockDataPlatformPayload> mockDataPlatformPayloadPublisher(
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
    public MessagePublisher<OrderCreatedPayload> orderCreatedPayloadPublisher(
            KafkaTemplate<String, OrderCreatedPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<ProductTotalPriceCompletedPayload> productTotalPriceCompletedPayloadPublisher(
            KafkaTemplate<String, ProductTotalPriceCompletedPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<CouponApplyCompletedPayload> couponApplyCompletedPayloadPublisher(
            KafkaTemplate<String, CouponApplyCompletedPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<PointUsedCompletedPayload> pointUsedCompletedPayloadPublisher(
            KafkaTemplate<String, PointUsedCompletedPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<PaymentCompletedPayload> paymentCompletedPayloadPublisher(
            KafkaTemplate<String, PaymentCompletedPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<ProductTotalPriceFailRollbackPayload> productTotalPriceFailRollbackPayloadPublisher(
            KafkaTemplate<String, ProductTotalPriceFailRollbackPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<CouponUseRollbackPayload> applyCouponFailedPayloadMessagePublisher(
            KafkaTemplate<String, CouponUseRollbackPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<PointUseRollbackPayload> pointUseRollbackPayloadMessagePublisher(
            KafkaTemplate<String, PointUseRollbackPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }

    @Bean
    public MessagePublisher<PaymentRollbackPayload> paymentRollbackPayloadMessagePublisher(
            KafkaTemplate<String, PaymentRollbackPayload> kafkaTemplate
    ) {
        return new KafkaProducer<>(kafkaTemplate);
    }
}
