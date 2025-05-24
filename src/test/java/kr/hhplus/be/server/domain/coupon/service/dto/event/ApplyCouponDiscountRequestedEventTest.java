package kr.hhplus.be.server.domain.coupon.service.dto.event;

import kr.hhplus.be.server.domain.coupon.dto.event.ApplyCouponDiscountRequestedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplyCouponDiscountRequestedEventTest {

    @Test
    @DisplayName("성공: 유효한 값으로 생성")
    void create_success() {
        ApplyCouponDiscountRequestedEvent event = new ApplyCouponDiscountRequestedEvent(
                1L, 2L, 3L, 1000L
        );

        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.userId()).isEqualTo(2L);
        assertThat(event.couponId()).isEqualTo(3L);
        assertThat(event.totalPrice()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("성공: couponId가 null인 경우 허용")
    void create_success_couponId_null() {
        ApplyCouponDiscountRequestedEvent event = new ApplyCouponDiscountRequestedEvent(
                1L, 2L, null, 1000L
        );

        assertThat(event.couponId()).isNull();
    }

    @Test
    @DisplayName("실패: orderId가 null이면 예외 발생")
    void create_fail_orderId_null() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountRequestedEvent(null, 1L, 1L, 1000L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 1 미만이면 예외 발생")
    void create_fail_orderId_invalid() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountRequestedEvent(0L, 1L, 1L, 1000L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 null이면 예외 발생")
    void create_fail_userId_null() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountRequestedEvent(1L, null, 1L, 1000L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만이면 예외 발생")
    void create_fail_userId_invalid() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountRequestedEvent(1L, 0L, 1L, 1000L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: couponId가 1 미만이면 예외 발생 (null 제외)")
    void create_fail_couponId_invalid() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountRequestedEvent(1L, 1L, 0L, 1000L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponId는 1 이상이거나 null이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 null이면 예외 발생")
    void create_fail_totalPrice_null() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountRequestedEvent(1L, 1L, 1L, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 음수면 예외 발생")
    void create_fail_totalPrice_negative() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountRequestedEvent(1L, 1L, 1L, -100L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }
}
