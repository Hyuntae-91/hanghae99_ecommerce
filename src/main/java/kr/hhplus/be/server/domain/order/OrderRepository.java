package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.model.Order;

public interface OrderRepository {
    Order save(Order order);

    Order getById(Long id);
}
