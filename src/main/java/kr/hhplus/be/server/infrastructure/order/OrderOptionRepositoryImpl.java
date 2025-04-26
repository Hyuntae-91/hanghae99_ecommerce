package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import kr.hhplus.be.server.infrastructure.order.repository.OrderOptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderOptionRepositoryImpl  implements OrderOptionRepository {

    private final OrderOptionJpaRepository orderOptionJpaRepository;

    @Override
    public OrderOption findWithLockById(Long id) {
        return orderOptionJpaRepository.findWithLockById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderOption not found. id = " + id));
    }

    @Override
    public List<OrderOption> findByProductId(Long productId) {
        return orderOptionJpaRepository.findByProductId(productId);
    }

    @Override
    public OrderOption findById(Long optionId) {
        return orderOptionJpaRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderOption not found. id = " + optionId));
    }

    @Override
    public OrderOption save(OrderOption orderOption) {
        return orderOptionJpaRepository.save(orderOption);
    }
}
