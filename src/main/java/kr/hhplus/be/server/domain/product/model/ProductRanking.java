package kr.hhplus.be.server.domain.product.model;

import kr.hhplus.be.server.domain.product.model.mapper.ProductScoreMapper;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;
import java.util.stream.Collectors;


public class ProductRanking {

    private final List<ProductScore> scores;

    public ProductRanking(List<ProductScore> scores) {
        this.scores = scores;
    }

    public List<ProductScore> scores() {
        return scores;
    }

    public List<ProductScore> topN(int n) {
        return scores.stream()
                .sorted(Comparator.comparingDouble(ProductScore::score).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public List<ProductScore> scoresBelowThreshold() {
        return scores.stream()
                .filter(ProductScore::isBelowThreshold)
                .collect(Collectors.toList());
    }

    public ProductRanking decayAll() {
        List<ProductScore> decayed = scores.stream()
                .map(ProductScore::decay)
                .collect(Collectors.toList());
        return new ProductRanking(decayed);
    }

    /**
     * 여러 일자의 snapshot 데이터를 기반으로 ProductRanking 생성
     */
    public static ProductRanking fromSnapshots(List<Set<ZSetOperations.TypedTuple<Object>>> snapshots) {
        Map<Long, Double> aggregation = new HashMap<>();

        for (Set<ZSetOperations.TypedTuple<Object>> snapshot : snapshots) {
            if (snapshot == null || snapshot.isEmpty()) {
                continue;
            }
            List<ProductScore> dayScores = ProductScoreMapper.fromRedisTypedTuples(snapshot);
            for (ProductScore score : dayScores) {
                aggregation.merge(score.productId(), score.score(), Double::sum);
            }
        }

        List<ProductScore> aggregatedScores = aggregation.entrySet().stream()
                .map(e -> new ProductScore(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new ProductRanking(aggregatedScores);
    }

    /**
     * 미리 합산된 Map을 기반으로 ProductRanking 생성
     */
    public static ProductRanking fromAggregation(Map<Long, Double> aggregation) {
        if (aggregation == null || aggregation.isEmpty()) {
            return new ProductRanking(List.of());
        }
        List<ProductScore> aggregatedScores = aggregation.entrySet().stream()
                .map(e -> new ProductScore(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        return new ProductRanking(aggregatedScores);
    }

    public ProductRanking slice(int page, int size) {
        if (scores == null || scores.isEmpty()) {
            return new ProductRanking(List.of());
        }

        int fromIndex = Math.min(page * size, scores.size());
        int toIndex = Math.min(fromIndex + size, scores.size());

        List<ProductScore> slicedScores = scores.subList(fromIndex, toIndex);

        return new ProductRanking(slicedScores);
    }
}
