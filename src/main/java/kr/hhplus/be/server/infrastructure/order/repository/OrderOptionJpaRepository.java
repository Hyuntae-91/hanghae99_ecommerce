package kr.hhplus.be.server.infrastructure.order.repository;

import kr.hhplus.be.server.domain.order.model.OrderOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderOptionJpaRepository extends JpaRepository<OrderOption, Long> {
    List<OrderOption> findByProductId(Long productId);
}