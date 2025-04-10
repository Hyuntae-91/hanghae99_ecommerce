package kr.hhplus.be.server.domain.order.service.model;

import kr.hhplus.be.server.domain.order.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("성공: Order 객체가 정상적으로 생성")
    void createOrder_doesNotThrow() {
        assertThatCode(() -> {
            Order order = Order.create(1L, 10000L, 2, 1L);
            assertThat(order.getUserId()).isEqualTo(1L);
            assertThat(order.getTotalPrice()).isEqualTo(10000L);
            assertThat(order.getQuantity()).isEqualTo(2);
            assertThat(order.getCouponIssueId()).isEqualTo(1L);
            assertThat(order.getState()).isEqualTo(0);  // 초기 state는 0 (생성)
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("성공: cancel() 호출 시 상태가 -1로 변경")
    void cancelOrder_changesStateToMinusOne() {
        // given
        Order order = Order.create(1L, 10000L, 2, 1L);

        // when
        order.cancel();

        // then
        assertThat(order.getState()).isEqualTo(-1);  // 상태가 -1로 변경되어야 함
    }

    @Test
    @DisplayName("성공: applyTotalPrice() 호출 시 totalPrice가 업데이트됨")
    void applyTotalPrice_updatesTotalPrice() {
        // given
        Order order = Order.create(1L, 10000L, 2, 1L);

        // when
        order.applyTotalPrice(12000L);

        // then
        assertThat(order.getTotalPrice()).isEqualTo(12000L);  // totalPrice가 12000으로 업데이트 되어야 함
    }

    @Test
    @DisplayName("성공: applyCoupon() 호출 시 couponIssueId가 업데이트됨")
    void applyCoupon_updatesCouponIssueId() {
        // given
        Order order = Order.create(1L, 10000L, 2, 1L);

        // when
        order.applyCoupon(2L);

        // then
        assertThat(order.getCouponIssueId()).isEqualTo(2L);  // couponIssueId가 2로 업데이트 되어야 함
    }

    @Test
    @DisplayName("실패: Order 객체 생성 시 필수 값이 누락되면 예외 발생")
    void createOrder_missingFields_throwsException() {
        assertThatThrownBy(() -> Order.create(null, 10000L, 2, 1L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Order.create(1L, null, 2, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
