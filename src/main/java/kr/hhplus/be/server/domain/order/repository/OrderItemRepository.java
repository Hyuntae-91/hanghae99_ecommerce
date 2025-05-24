package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);

    OrderItem findById(Long id);

    List<OrderItem> findByIds(List<Long> orderItemIds);

    List<OrderItem> findCartByUserId(Long userId);

    List<OrderItem> findCartByUserIdAndOptionIds(Long userId, List<Long> optionIds);

    List<OrderItem> saveAll(List<OrderItem> orderItems);
}