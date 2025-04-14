package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderOptionRepositoryImpl  implements OrderOptionRepository {
    @Override
    public OrderOption getById(Long id) {
        return OrderOption.builder().id(id).build();
    }

    @Override
    public void save(OrderOption orderOption) {}
}
