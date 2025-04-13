package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.product.dto.GetProductsRepositoryRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product findById(Long id);

    List<Product> findByIds(List<Long> ids);

    List<Product> findByStateNotIn(int page, int size, String sort, List<Integer> excludeStates);

    List<Product> findAll();

    List<Product> findPopularTop5();

    void recalculateBestProducts();
}
