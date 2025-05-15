package kr.hhplus.be.server.domain.product.service.model;

import kr.hhplus.be.server.domain.product.model.ProductScore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductScoreTest {

    @Test
    @DisplayName("임계값 이하 여부 판별 - 30점 이하")
    void testIsBelowThreshold() {
        ProductScore score1 = new ProductScore(1L, 30.0);
        ProductScore score2 = new ProductScore(2L, 15.0);
        ProductScore score3 = new ProductScore(3L, 50.0);

        assertThat(score1.isBelowThreshold()).isTrue();  // 30.0 → true
        assertThat(score2.isBelowThreshold()).isTrue();  // 15.0 → true
        assertThat(score3.isBelowThreshold()).isFalse(); // 50.0 → false
    }

    @Test
    @DisplayName("점수 감쇠 적용")
    void testDecay() {
        ProductScore score = new ProductScore(1L, 80.0);

        ProductScore decayedScore = score.decay();

        assertThat(decayedScore.score()).isEqualTo(40.0);  // 80 * 0.5
        assertThat(decayedScore.productId()).isEqualTo(1L); // productId는 변하지 않음
    }
}