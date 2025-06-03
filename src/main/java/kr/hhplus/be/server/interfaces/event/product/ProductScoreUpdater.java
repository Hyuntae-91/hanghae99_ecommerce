package kr.hhplus.be.server.interfaces.event.product;

import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.product.mapper.ProductMapper;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentCompletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductScoreUpdater {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @KafkaListener(topics = Topics.PAYMENT_COMPLETE_TOPIC, groupId = Groups.PAYMENT_COMPLETE_PRODUCT_SCORES_GROUP)
    public void consume(@Payload PaymentCompletedPayload event) {
        productService.updateProductsScore(event.productIds());
    }
}
