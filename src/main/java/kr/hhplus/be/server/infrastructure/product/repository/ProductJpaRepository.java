package kr.hhplus.be.server.infrastructure.product.repository;

import kr.hhplus.be.server.domain.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    Optional<Product> findById(Long id);

    List<Product> findByIdIn(List<Long> ids);

    Page<Product> findByStateNotIn(List<Integer> excludeStates, Pageable pageable);

    @Query(value = """
        SELECT p.*
        FROM product p
        JOIN (
          SELECT oi.product_id
          FROM order_item oi
          WHERE oi.order_id IN (
              SELECT o.id
              FROM `order` o
              WHERE o.state = 1
                AND o.created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY)
          )
          GROUP BY oi.product_id
          ORDER BY SUM(oi.quantity) DESC
          LIMIT 5
        ) best ON p.id = best.product_id;
    """, nativeQuery = true)
    List<Product> findTop5PopularProducts();

    // 첫 페이지용
    @Query("""
           SELECT p FROM Product p
           WHERE p.state NOT IN :states
           ORDER BY p.id DESC
           """)
    List<Product> findFirstPage(
            @Param("states") List<Integer> states,
            Pageable pageable
    );

    // cursor 이후(slice) 페이지용
    @Query("""
           SELECT p FROM Product p
           WHERE p.state NOT IN :states
             AND p.id < :cursorId
           ORDER BY p.id DESC
           """)
    List<Product> findSliceAfterCursor(
            @Param("cursorId") Long cursorId,
            @Param("states")   List<Integer> states,
            Pageable pageable
    );

}
