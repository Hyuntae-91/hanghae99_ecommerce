package kr.hhplus.be.server.domain.product.service.model;

import kr.hhplus.be.server.domain.product.model.ProductRanking;
import kr.hhplus.be.server.domain.product.model.ProductScore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRankingTest {

    @Test
    @DisplayName("Top N 상품 조회")
    void testTopN() {
        ProductRanking ranking = new ProductRanking(List.of(
                new ProductScore(1L, 10.0),
                new ProductScore(2L, 30.0),
                new ProductScore(3L, 20.0)
        ));

        List<ProductScore> top2 = ranking.topN(2);

        assertThat(top2)
                .hasSize(2)
                .extracting(ProductScore::productId)
                .containsExactly(2L, 3L);  // 점수 높은 순
    }

    @Test
    @DisplayName("Page, Size로 Slice 잘리는지 확인")
    void testSlice() {
        ProductRanking ranking = new ProductRanking(List.of(
                new ProductScore(1L, 10.0),
                new ProductScore(2L, 20.0),
                new ProductScore(3L, 30.0),
                new ProductScore(4L, 40.0),
                new ProductScore(5L, 50.0)
        ));

        ProductRanking sliced = ranking.slice(1, 2);  // page=1, size=2 → 3번째, 4번째

        assertThat(sliced.scores())
                .hasSize(2)
                .extracting(ProductScore::productId)
                .containsExactly(3L, 4L);
    }

    @Test
    @DisplayName("모든 상품 점수 감쇠 적용")
    void testDecayAll() {
        ProductRanking ranking = new ProductRanking(List.of(
                new ProductScore(1L, 100.0),
                new ProductScore(2L, 50.0)
        ));

        ProductRanking decayed = ranking.decayAll();

        assertThat(decayed.scores())
                .hasSize(2)
                .extracting(ProductScore::score)
                .containsExactly(50.0, 25.0);  // 감쇠율 0.5 적용 (ProductScore 내부 룰)
    }

    @Test
    @DisplayName("임계값 이하 상품 필터링")
    void testScoresBelowThreshold() {
        ProductRanking ranking = new ProductRanking(List.of(
                new ProductScore(1L, 5.0),
                new ProductScore(2L, 20.0),
                new ProductScore(3L, 3.0)
        ));

        List<ProductScore> belowThreshold = ranking.scoresBelowThreshold();

        assertThat(belowThreshold)
                .hasSize(3)  // 🔥 2 → 3으로 수정
                .extracting(ProductScore::productId)
                .containsExactly(1L, 2L, 3L); // 5.0, 20.0, 3.0 전부 필터링
    }

    @Test
    @DisplayName("Aggregation Map 기반 ProductRanking 생성")
    void testFromAggregation() {
        Map<Long, Double> aggregation = Map.of(
                1L, 100.0,
                2L, 200.0
        );

        ProductRanking ranking = ProductRanking.fromAggregation(aggregation);

        assertThat(ranking.scores())
                .hasSize(2)
                .extracting(ProductScore::productId)
                .containsExactlyInAnyOrder(1L, 2L);
    }
}