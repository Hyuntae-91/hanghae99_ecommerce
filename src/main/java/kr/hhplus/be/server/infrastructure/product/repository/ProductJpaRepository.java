package kr.hhplus.be.server.infrastructure.product.repository;

import kr.hhplus.be.server.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    // TODO: 관련 동작 쿼리 수정 예정
    List<Product> findTop5ByOrderByScoreDesc();

}
