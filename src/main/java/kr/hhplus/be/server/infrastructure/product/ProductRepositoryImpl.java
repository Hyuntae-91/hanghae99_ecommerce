package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.mapper.ProductScoreMapper;
import kr.hhplus.be.server.domain.product.model.ProductRanking;
import kr.hhplus.be.server.domain.product.model.ProductScore;
import kr.hhplus.be.server.domain.product.model.vo.ProductRankingKey;
import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Product findById(Long id) {
        return productJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
    }

    @Override
    public List<Product> findByIds(List<Long> ids) {
        return productJpaRepository.findByIdIn(ids);
    }

    @Override
    public List<Product> findByStateNotIn(int page, int size, String sort, List<Integer> excludeStates) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort));
        return productJpaRepository.findByStateNotIn(excludeStates, pageable).getContent();
    }

    @Override
    public List<Product> findByStateNotInCursor(Long cursorId, int size, String sort, List<Integer> excludeStates) {
        Sort sortSpec = Sort.by(Sort.Direction.ASC, sort);
        Pageable pageable = PageRequest.of(0, size, sortSpec);

        if (cursorId == null) {
            // 첫 페이지
            return productJpaRepository.findFirstPage(excludeStates, pageable);
        }
        // cursorId 기준 이후(slice) 페이지
        return productJpaRepository.findSliceAfterCursor(cursorId, excludeStates, pageable);
    }

    @Override
    public List<Product> getDailyPageProducts(int page, int size) {
        ProductRankingKey rankingKey = ProductRankingKey.ofDaily(LocalDate.now());
        Set<ZSetOperations.TypedTuple<Object>> rankingData = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankingKey.value(), 0, ProductRanking.MAX_RANK_SIZE - 1);

        ProductRanking ranking = ProductRanking.fromSnapshots(List.of(Objects.requireNonNull(rankingData)));
        ProductRanking sliced = ranking.slice(page, size);

        return productJpaRepository.findByIdIn(sliced.productIds());
    }

    @Override
    public List<Product> getWeeklyPageProducts(int page, int size) {
        String rankingKey = ProductRankingKey.weekKey();
        Set<ZSetOperations.TypedTuple<Object>> rankingData = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankingKey, 0, ProductRanking.MAX_RANK_SIZE - 1);

        ProductRanking ranking = ProductRanking.fromSnapshots(List.of(Objects.requireNonNull(rankingData)));
        ProductRanking sliced = ranking.slice(page, size);

        return productJpaRepository.findByIdIn(sliced.productIds());
    }

    @Override
    public void expireAt(String key, Instant expireAt) {
        redisTemplate.expireAt(key, expireAt);
    }

    @Override
    public boolean copyKey(String originalKey, String copyKey) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(originalKey))) {
            log.info("No current key found: {}", originalKey);
            return false;
        }
        redisTemplate.opsForZSet().unionAndStore(originalKey, Set.of(), copyKey);
        return true;
    }

    @Override
    public List<ProductScore> getTopN(String key, int n) {
        Set<ZSetOperations.TypedTuple<Object>> topN = redisTemplate.opsForZSet().reverseRangeWithScores(
                key, 0, n - 1
        );
        if (topN == null || topN.isEmpty()) {
            return List.of();
        }

        return ProductScoreMapper.fromRedisTypedTuples(topN);
    }

    @Override
    public List<ProductScore> getTopNProductsFromLast7Days(int n) {
        LocalDate today = LocalDate.now();
        List<Set<ZSetOperations.TypedTuple<Object>>> snapshots = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String snapshotKey = ProductRankingKey.ofDaily(day).value();
            Set<ZSetOperations.TypedTuple<Object>> dailySnapshot = redisTemplate.opsForZSet().reverseRangeWithScores(
                    snapshotKey, 0, n - 1
            );
            snapshots.add(dailySnapshot);
        }

        // 스냅샷 합산 → ProductRanking 생성
        ProductRanking aggregatedRanking = ProductRanking.fromSnapshots(snapshots);
        return aggregatedRanking.topN(n);
    }

    @Override
    public void replaceNewRanking(String key, List<ProductScore> scores) {
        redisTemplate.delete(key);  // 기존 키 삭제
        for (ProductScore score : scores) {
            redisTemplate.opsForZSet().add(key, score.productId().toString(), score.score());
        }
    }

    @Override
    public void updateProductsScore(List<Long> productIds) {
        for (Long productId : productIds) {
            redisTemplate.opsForZSet().incrementScore(ProductRankingKey.currentKey(), productId.toString(), ProductScore.SALE_SCORE);
            log.info("[ProductScoreUpdateEventListener] Increased score for productId={} by {}", productId, ProductScore.SALE_SCORE);
        }
    }
}
