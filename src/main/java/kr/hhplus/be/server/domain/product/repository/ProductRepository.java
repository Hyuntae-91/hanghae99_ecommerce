package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.model.ProductRanking;
import kr.hhplus.be.server.domain.product.model.ProductScore;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface ProductRepository {

    Product findById(Long id);

    List<Product> findByIds(List<Long> ids);

    List<Product> findByStateNotIn(int page, int size, String sort, List<Integer> excludeStates);

    List<Product> findByStateNotInCursor(Long cursorId, int size, String sort, List<Integer> excludeStates);

    List<Product> getDailyPageProducts(int page, int size);

    List<Product> getWeeklyPageProducts(int page, int size);

    List<ProductScore> getTopN(String key, int n);

    List<ProductScore> getTopNProductsFromLast7Days(int n);

    void expireAt(String key, Instant expireAt);

    boolean copyKey(String originalKey, String copyKey);

    void replaceNewRanking(String key, List<ProductScore> scores);

    void updateProductsScore(List<Long> productIds);
}
