package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.common.exception.ResourceNotFoundException;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product findById(Long id) {
        return productJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
    }

    @Override
    public List<Product> findByIds(List<Long> ids) {
        return productJpaRepository.findAllById(ids);
    }

    @Override
    public List<Product> findByStateNotIn(int page, int size, String sort, List<Integer> excludeStates) {
        return null;  // TODO : 추후 구현
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    @Override
    public List<Product> findPopularTop5() {
        //return productJpaRepository.findTop5ByOrderByScoreDesc(); // 가정: score 필드가 있음
        return null;
    }

    @Override
    public void recalculateBestProducts() {
        // TODO: 추후 통계 계산 쿼리로 대체
    }
}
