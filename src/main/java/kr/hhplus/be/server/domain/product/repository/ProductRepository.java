package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.Product;

import java.util.List;

public interface ProductRepository {

    Product findById(Long id);

    List<Product> findByIds(List<Long> ids);

    List<Product> findByStateNotIn(int page, int size, String sort, List<Integer> excludeStates);

    List<Product> findAll();

    List<Product> findPopularTop5();

    void recalculateBestProducts();
}
