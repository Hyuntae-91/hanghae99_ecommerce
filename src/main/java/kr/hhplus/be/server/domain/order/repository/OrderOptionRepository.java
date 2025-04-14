package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.OrderOption;

public interface OrderOptionRepository {
    OrderOption getById(Long id);
    void save(OrderOption orderOption);
}
