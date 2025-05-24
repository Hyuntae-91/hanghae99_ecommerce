package kr.hhplus.be.server.domain.product.service.mapper;

import kr.hhplus.be.server.domain.product.mapper.ProductScoreMapper;
import kr.hhplus.be.server.domain.product.model.ProductScore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductScoreMapperTest {

    @Test
    @DisplayName("성공: Redis TypedTuple을 ProductScore 리스트로 변환")
    void fromRedisTypedTuples_success() {
        ZSetOperations.TypedTuple<Object> tuple1 = mock(ZSetOperations.TypedTuple.class);
        ZSetOperations.TypedTuple<Object> tuple2 = mock(ZSetOperations.TypedTuple.class);

        when(tuple1.getValue()).thenReturn("1");
        when(tuple1.getScore()).thenReturn(10.0);
        when(tuple2.getValue()).thenReturn("2");
        when(tuple2.getScore()).thenReturn(20.0);

        Set<ZSetOperations.TypedTuple<Object>> entries = new HashSet<>();
        entries.add(tuple1);
        entries.add(tuple2);

        List<ProductScore> result = ProductScoreMapper.fromRedisTypedTuples(entries);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductScore::productId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("성공: ProductScore 리스트에서 productId만 추출")
    void toProductIdSet_success() {
        List<ProductScore> scores = List.of(
                new ProductScore(1L, 10.0),
                new ProductScore(2L, 20.0)
        );

        List<Long> result = ProductScoreMapper.toProductIdSet(scores);

        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("빈 입력 처리: null 또는 빈 Set을 fromRedisTypedTuples로 변환 시 빈 리스트 반환")
    void fromRedisTypedTuples_empty() {
        assertThat(ProductScoreMapper.fromRedisTypedTuples(null)).isEmpty();
        assertThat(ProductScoreMapper.fromRedisTypedTuples(Set.of())).isEmpty();
    }

    @Test
    @DisplayName("빈 입력 처리: null 또는 빈 리스트를 toProductIdSet으로 변환 시 빈 리스트 반환")
    void toProductIdSet_empty() {
        assertThat(ProductScoreMapper.toProductIdSet(null)).isEmpty();
        assertThat(ProductScoreMapper.toProductIdSet(List.of())).isEmpty();
    }
}
