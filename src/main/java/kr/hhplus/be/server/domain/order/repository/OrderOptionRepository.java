package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.OrderOption;

import java.util.List;

public interface OrderOptionRepository {
    OrderOption getById(Long id);
    OrderOption save(OrderOption orderOption);
    List<OrderOption> findByProductId(Long productId);
    OrderOption findById(Long optionId);
}
