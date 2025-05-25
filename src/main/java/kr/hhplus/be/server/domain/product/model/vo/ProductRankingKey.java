package kr.hhplus.be.server.domain.product.model.vo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ProductRankingKey {

    private final String value;

    private ProductRankingKey(String value) {
        this.value = value;
    }

    public static ProductRankingKey ofDaily(LocalDate date) {
        String key = "product:score:" + date.format(DateTimeFormatter.ISO_DATE);
        return new ProductRankingKey(key);
    }

    public static String tempKey(LocalDate date) {
        return "product:score:temp:" + date.format(DateTimeFormatter.ISO_DATE);
    }

    public static String currentKey() {
        return "product:score:current";
    }

    public static String weekKey() { return "product:score:week"; }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    // equals & hashCode 구현 (VO 특성상 값 비교)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductRankingKey that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
