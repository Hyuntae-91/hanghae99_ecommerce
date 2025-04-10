package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.model.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);

    List<OrderItem> findByIds(List<Long> orderItemIds);

    List<OrderItem> findAllByOrderId(Long orderId);

    List<OrderItem> findCartByUserId(Long userId);

    void saveAll(List<OrderItem> orderItems);
}