package kr.hhplus.be.server.infrastructure.order.repository;

import kr.hhplus.be.server.domain.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
