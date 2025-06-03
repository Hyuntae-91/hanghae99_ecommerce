package kr.hhplus.be.server.interfaces.event.order;

import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.order.mapper.OrderMapper;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentCompletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderUpdateProcessor {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @KafkaListener(topics = Topics.PAYMENT_COMPLETE_TOPIC, groupId = Groups.PAYMENT_COMPLETE_ORDER_UPDATE_GROUP)
    public void consume(@Payload PaymentCompletedPayload event) {
        orderService.updateOrder(orderMapper.toUpdateOrderRequest(event));
    }
}
