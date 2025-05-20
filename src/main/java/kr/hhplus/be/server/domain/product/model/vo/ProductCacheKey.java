package kr.hhplus.be.server.domain.product.model.vo;

import java.util.Objects;

public class ProductCacheKey {

    private final String value;

    private ProductCacheKey(String value) {
        this.value = value;
    }

    public static ProductCacheKey dailyBest(int page, int size) {
        String key = "productDailyBest::dailyBest::page=" + page + ":size=" + size;
        return new ProductCacheKey(key);
    }

    public static ProductCacheKey weeklyBest(int page, int size) {
        String key = "productWeeklyBest::weeklyBest::page=" + page + ":size=" + size;
        return new ProductCacheKey(key);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCacheKey that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
