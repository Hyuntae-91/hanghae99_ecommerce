package kr.hhplus.be.server.domain.coupon.service.dto.event;

import kr.hhplus.be.server.domain.coupon.dto.event.ApplyCouponDiscountCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class ApplyCouponDiscountCompletedEventTest {

    @Test
    @DisplayName("성공: 유효한 값으로 객체 생성")
    void success_create_event() {
        ApplyCouponDiscountCompletedEvent event = new ApplyCouponDiscountCompletedEvent(
                1L, 2L, 3L, 1000L, List.of(10L, 20L)
        );

        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.userId()).isEqualTo(2L);
        assertThat(event.couponId()).isEqualTo(3L);
        assertThat(event.finalPrice()).isEqualTo(1000L);
        assertThat(event.productIds()).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("실패: orderId가 null이면 예외 발생")
    void fail_when_orderId_null() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(null, 1L, 1L, 1000L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 1 미만이면 예외 발생")
    void fail_when_orderId_invalid() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(0L, 1L, 1L, 1000L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 null이면 예외 발생")
    void fail_when_userId_null() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(1L, null, 1L, 1000L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만이면 예외 발생")
    void fail_when_userId_invalid() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(1L, 0L, 1L, 1000L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: couponId가 1 미만 (null은 허용)")
    void fail_when_couponId_invalid() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(1L, 1L, 0L, 1000L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponId는 1 이상이거나 null이어야 합니다.");
    }

    @Test
    @DisplayName("성공: couponId가 null이면 허용")
    void success_when_couponId_null() {
        ApplyCouponDiscountCompletedEvent event = new ApplyCouponDiscountCompletedEvent(
                1L, 1L, null, 1000L, List.of(1L)
        );

        assertThat(event.couponId()).isNull();
    }

    @Test
    @DisplayName("실패: finalPrice가 null이면 예외 발생")
    void fail_when_finalPrice_null() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(1L, 1L, 1L, null, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finalPrice는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("실패: productIds가 null이면 예외 발생")
    void fail_when_productIds_null() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(1L, 1L, 1L, 1000L, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productIds는 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: productIds가 비어있으면 예외 발생")
    void fail_when_productIds_empty() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(1L, 1L, 1L, 1000L, List.of())
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productIds는 null이거나 비어 있을 수 없습니다.");
    }
}
