package kr.hhplus.be.server.interfaces.event.product;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.product.mapper.ProductMapper;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponUseRollbackPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.OrderCreatedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductTotalPriceCompletedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductTotalPriceFailRollbackPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedTotalPriceProcessor {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final MessagePublisher<ProductTotalPriceCompletedPayload> productTotalPriceCompletedPayloadPublisher;
    private final MessagePublisher<CouponUseRollbackPayload> couponUseRollbackPayloadMessagePublisher;

    @KafkaListener(topics = Topics.ORDER_CREATED_TOPIC, groupId = Groups.ORDER_CREATED_GROUP)
    public void consume(@Payload OrderCreatedPayload event) {
        try {
            ProductTotalPriceResponse response = productService.calculateTotalPrice(event.items());

            productTotalPriceCompletedPayloadPublisher.publish(
                    Topics.PRODUCT_TOTAL_PRICE_TOPIC,
                    null,
                    productMapper.toProductTotalPriceCompletedPayload(event, response)
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            couponUseRollbackPayloadMessagePublisher.publish(
                    Topics.PRODUCT_TOTAL_PRICE_FAIL_ROLLBACK,
                    null,
                    productMapper.toProductTotalPriceFailRollbackPayload(event)
            );
        }
    }
}
