package kr.hhplus.be.server.domain.product.model.mapper;

import kr.hhplus.be.server.domain.product.model.ProductScore;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductScoreMapper {

    private ProductScoreMapper() {}

    /**
     * Redis TypedTuple(Object) -> ProductScore 리스트 변환
     */
    public static List<ProductScore> fromRedisTypedTuples(Set<ZSetOperations.TypedTuple<Object>> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        return entries.stream()
                .filter(entry -> entry.getValue() != null && entry.getScore() != null)
                .map(entry -> new ProductScore(Long.parseLong(entry.getValue().toString()), entry.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * ProductScore 리스트 -> productId(Long) Set 변환
     */
    public static Set<Long> toProductIdSet(List<ProductScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return Set.of();
        }
        return scores.stream()
                .map(ProductScore::productId)
                .collect(Collectors.toSet());
    }

    /**
     * Map<productId, score> -> Top N 추출
     */
    public static List<Map.Entry<Long, Double>> toTopN(Map<Long, Double> aggregation, int topN) {
        if (aggregation == null || aggregation.isEmpty()) {
            return List.of();
        }
        return aggregation.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .toList();
    }

    public static List<Long> toProductIdList(Set<ZSetOperations.TypedTuple<Object>> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        return entries.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
    }

    public static List<Long> toProductIdList(List<ProductScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return List.of();
        }
        return scores.stream()
                .map(ProductScore::productId)
                .toList();
    }
}