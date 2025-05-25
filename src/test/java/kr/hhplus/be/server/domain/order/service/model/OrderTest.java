package kr.hhplus.be.server.domain.order.service.model;

import kr.hhplus.be.server.domain.order.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("성공: Order 객체가 정상적으로 생성됨")
    void createOrder_success() {
        Order order = Order.create(1L, 10000L);

        assertThat(order.getUserId()).isEqualTo(1L);
        assertThat(order.getTotalPrice()).isEqualTo(10000L);
        assertThat(order.getCouponIssueId()).isEqualTo(-1L);  // 기본값
        assertThat(order.getState()).isEqualTo(0);  // 기본 생성 상태
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("성공: applyTotalPrice() 호출 시 totalPrice가 업데이트됨")
    void applyTotalPrice_updates_value() {
        Order order = Order.create(1L, 10000L);

        order.applyTotalPrice(15000L);

        assertThat(order.getTotalPrice()).isEqualTo(15000L);
    }

    @Test
    @DisplayName("성공: applyCoupon() 호출 시 couponIssueId가 업데이트됨")
    void applyCoupon_updates_value() {
        Order order = Order.create(1L, 10000L);

        order.applyCoupon(3L);

        assertThat(order.getCouponIssueId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("성공: updateState() 호출 시 state가 변경됨")
    void updateState_changes_value() {
        Order order = Order.create(1L, 10000L);

        order.updateState(1);

        assertThat(order.getState()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패: Order 생성 시 userId가 null이면 예외 발생")
    void createOrder_fails_when_userId_is_null() {
        assertThatThrownBy(() -> Order.create(null, 10000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId와 totalPrice는 필수 값입니다.");
    }

    @Test
    @DisplayName("실패: Order 생성 시 totalPrice가 null이면 예외 발생")
    void createOrder_fails_when_totalPrice_is_null() {
        assertThatThrownBy(() -> Order.create(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId와 totalPrice는 필수 값입니다.");
    }

    @Test
    @DisplayName("성공: Order.of 정적 메서드로 생성")
    void createOrder_using_of() {
        Order order = Order.of(2L, 20000L, 1);

        assertThat(order.getUserId()).isEqualTo(2L);
        assertThat(order.getTotalPrice()).isEqualTo(20000L);
        assertThat(order.getState()).isEqualTo(1);
        assertThat(order.getCouponIssueId()).isEqualTo(-1L);
    }
}
