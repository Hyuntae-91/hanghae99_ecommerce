package kr.hhplus.be.server.domain.coupon.service.model;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CouponTest {

    @Test
    @DisplayName("성공: FIXED 타입 쿠폰 할인 계산")
    void calculateDiscount_fixedType() {
        // given
        Coupon coupon = Coupon.builder()
                .id(1L)
                .type(CouponType.FIXED)
                .description("5000원 할인")
                .discount(5000)
                .quantity(100)
                .state(1)
                .expirationDays(30)
                .createdAt("2025-05-15T00:00:00")
                .updatedAt("2025-05-15T00:00:00")
                .build();

        // when
        long discount = coupon.calculateDiscount(10000L);

        // then
        assertThat(discount).isEqualTo(5000L);
    }

    @Test
    @DisplayName("성공: PERCENT 타입 쿠폰 할인 계산")
    void calculateDiscount_percentType() {
        // given
        Coupon coupon = Coupon.builder()
                .id(2L)
                .type(CouponType.PERCENT)
                .description("10% 할인")
                .discount(10)
                .quantity(100)
                .state(1)
                .expirationDays(30)
                .createdAt("2025-05-15T00:00:00")
                .updatedAt("2025-05-15T00:00:00")
                .build();

        // when
        long discount = coupon.calculateDiscount(20000L);

        // then
        assertThat(discount).isEqualTo(2000L);
    }

    @Test
    @DisplayName("성공: FIXED 타입 쿠폰이 원래 가격보다 클 때 할인은 원래 가격까지")
    void calculateDiscount_fixedType_discountExceedsPrice() {
        // given
        Coupon coupon = Coupon.builder()
                .id(3L)
                .type(CouponType.FIXED)
                .description("15000원 할인")
                .discount(15000)
                .quantity(100)
                .state(1)
                .expirationDays(30)
                .createdAt("2025-05-15T00:00:00")
                .updatedAt("2025-05-15T00:00:00")
                .build();

        // when
        long discount = coupon.calculateDiscount(10000L);

        // then
        assertThat(discount).isEqualTo(10000L);
    }

    @Test
    @DisplayName("성공: PERCENT 타입 쿠폰이 100% 이상이어도 할인은 원래 가격까지")
    void calculateDiscount_percentType_discountExceeds100Percent() {
        // given
        Coupon coupon = Coupon.builder()
                .id(4L)
                .type(CouponType.PERCENT)
                .description("150% 할인")
                .discount(150)
                .quantity(100)
                .state(1)
                .expirationDays(30)
                .createdAt("2025-05-15T00:00:00")
                .updatedAt("2025-05-15T00:00:00")
                .build();

        // when
        long discount = coupon.calculateDiscount(20000L);

        // then
        assertThat(discount).isEqualTo(20000L);
    }
}

