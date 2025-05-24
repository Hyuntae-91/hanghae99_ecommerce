package kr.hhplus.be.server.domain.product.service.model.vo;

import kr.hhplus.be.server.domain.product.model.vo.ProductRankingKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRankingKeyTest {

    @Test
    @DisplayName("ofDaily 메서드는 ISO_DATE 형식의 키를 생성해야 한다")
    void testOfDaily() {
        LocalDate date = LocalDate.of(2025, 5, 23);
        ProductRankingKey key = ProductRankingKey.ofDaily(date);

        assertThat(key.value()).isEqualTo("product:score:2025-05-23");
    }

    @Test
    @DisplayName("tempKey 메서드는 temp 접두사를 포함한 키를 생성해야 한다")
    void testTempKey() {
        LocalDate date = LocalDate.of(2025, 5, 23);
        String key = ProductRankingKey.tempKey(date);

        assertThat(key).isEqualTo("product:score:temp:2025-05-23");
    }

    @Test
    @DisplayName("currentKey 메서드는 고정된 current 키를 반환해야 한다")
    void testCurrentKey() {
        assertThat(ProductRankingKey.currentKey()).isEqualTo("product:score:current");
    }

    @Test
    @DisplayName("weekKey 메서드는 고정된 week 키를 반환해야 한다")
    void testWeekKey() {
        assertThat(ProductRankingKey.weekKey()).isEqualTo("product:score:week");
    }

    @Test
    @DisplayName("equals는 값이 같으면 true를 반환해야 한다")
    void testEquals() {
        ProductRankingKey key1 = ProductRankingKey.ofDaily(LocalDate.of(2025, 5, 23));
        ProductRankingKey key2 = ProductRankingKey.ofDaily(LocalDate.of(2025, 5, 23));

        assertThat(key1).isEqualTo(key2);
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("toString은 value와 동일한 값을 반환해야 한다")
    void testToString() {
        ProductRankingKey key = ProductRankingKey.ofDaily(LocalDate.of(2025, 5, 23));
        assertThat(key.toString()).isEqualTo("product:score:2025-05-23");
    }
}
