package kr.hhplus.be.server.domain.product.event;

import kr.hhplus.be.server.domain.order.dto.event.OrderCreatedEvent;
import kr.hhplus.be.server.domain.product.mapper.ProductMapper;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventHandler {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final ApplicationEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void calculateTotalPriceHandler(OrderCreatedEvent event) {
        ProductTotalPriceResponse response = productService.calculateTotalPrice(
                productMapper.toProductTotalPriceRequestedEvent(event)
        );

        publisher.publishEvent(productMapper.toProductTotalPriceCompletedEvent(event, response));
    }
}
