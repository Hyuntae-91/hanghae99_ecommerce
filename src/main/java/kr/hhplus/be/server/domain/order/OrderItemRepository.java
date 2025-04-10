package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.model.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);

    List<OrderItem> findAllByOrderId(Long orderId);

    List<OrderItem> findCartByUserId(Long userId);
}