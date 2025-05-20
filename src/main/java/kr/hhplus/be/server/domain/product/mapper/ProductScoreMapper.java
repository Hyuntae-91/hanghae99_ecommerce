package kr.hhplus.be.server.domain.product.mapper;

import kr.hhplus.be.server.domain.product.model.ProductScore;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductScoreMapper {

    private ProductScoreMapper() {}

    public static List<ProductScore> fromRedisTypedTuples(Set<ZSetOperations.TypedTuple<Object>> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        return entries.stream()
                .filter(entry -> entry.getValue() != null && entry.getScore() != null)
                .map(entry -> new ProductScore(Long.parseLong(entry.getValue().toString()), entry.getScore()))
                .collect(Collectors.toList());
    }

    public static List<Long> toProductIdSet(List<ProductScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return List.of();
        }
        return scores.stream()
                .map(ProductScore::productId)
                .toList();
    }
}