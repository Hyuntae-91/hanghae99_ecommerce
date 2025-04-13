package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.domain.common.exception.ResourceNotFoundException;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.infrastructure.order.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Order getById(Long id) {
        return orderJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order Not Found"));
    }
}
