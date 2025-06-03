package kr.hhplus.be.server.interfaces.event.order;

import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.order.mapper.OrderMapper;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponUseRollbackPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductTotalPriceFailRollbackProcessor {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @KafkaListener(
            topics = Topics.PRODUCT_TOTAL_PRICE_FAIL_ROLLBACK,
            groupId = Groups.PRODUCT_TOTAL_PRICE_FAIL_ROLLBACK_GROUP
    )
    public void consume(@Payload CouponUseRollbackPayload event) {
        orderService.updateOrderStatus(orderMapper.toUpdateOrderStateRequest(event, -1));
        orderService.orderStockRollback(orderMapper.toOrderStockRollbackRequest(event));
    }
}
