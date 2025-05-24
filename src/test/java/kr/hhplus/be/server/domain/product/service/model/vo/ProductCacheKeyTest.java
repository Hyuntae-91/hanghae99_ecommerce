package kr.hhplus.be.server.domain.product.service.model.vo;

import kr.hhplus.be.server.domain.product.model.vo.ProductCacheKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCacheKeyTest {

    @Test
    @DisplayName("성공: dailyBest 키 생성 확인")
    void createDailyBestKey_success() {
        ProductCacheKey key = ProductCacheKey.dailyBest(1, 20);
        assertThat(key.value()).isEqualTo("productDailyBest::dailyBest::page=1:size=20");
        assertThat(key.toString()).isEqualTo("productDailyBest::dailyBest::page=1:size=20");
    }

    @Test
    @DisplayName("성공: weeklyBest 키 생성 확인")
    void createWeeklyBestKey_success() {
        ProductCacheKey key = ProductCacheKey.weeklyBest(2, 10);
        assertThat(key.value()).isEqualTo("productWeeklyBest::weeklyBest::page=2:size=10");
        assertThat(key.toString()).isEqualTo("productWeeklyBest::weeklyBest::page=2:size=10");
    }

    @Test
    @DisplayName("성공: equals, hashCode 테스트")
    void equalsAndHashCode_success() {
        ProductCacheKey key1 = ProductCacheKey.dailyBest(1, 20);
        ProductCacheKey key2 = ProductCacheKey.dailyBest(1, 20);
        ProductCacheKey key3 = ProductCacheKey.weeklyBest(1, 20);

        assertThat(key1).isEqualTo(key2);
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        assertThat(key1).isNotEqualTo(key3);
    }
}
