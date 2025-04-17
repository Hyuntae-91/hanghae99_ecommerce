package kr.hhplus.be.server.domain.payment.service.model;

import kr.hhplus.be.server.domain.payment.model.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Test
    @DisplayName("성공: of()로 Payment 객체 생성")
    void create_payment_success() {
        Long orderId = 1L;
        Integer status = 1;
        Long totalPrice = 5000L;

        Payment payment = Payment.of(orderId, status, totalPrice);

        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getState()).isEqualTo(status);
        assertThat(payment.getTotalPrice()).isEqualTo(totalPrice);
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("성공: cancel() 호출 시 상태가 -1로 변경되고 updatedAt 갱신")
    void cancel_payment_success() {
        Payment payment = Payment.of(1L, 1, 5000L);
        String oldUpdatedAt = payment.getUpdatedAt();

        // sleep to ensure updatedAt will be different
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignored
        }

        payment.cancel();

        assertThat(payment.getState()).isEqualTo(-1);
        assertThat(payment.getUpdatedAt()).isNotEqualTo(oldUpdatedAt);
    }

    @Test
    @DisplayName("성공: isCompleted()는 상태가 1이면 true 반환")
    void is_completed_success() {
        Payment payment = Payment.of(1L, 1, 1000L);
        assertThat(payment.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("성공: isCancelled()는 상태가 -1이면 true 반환")
    void is_cancelled_success() {
        Payment payment = Payment.of(1L, -1, 1000L);
        assertThat(payment.isCancelled()).isTrue();
    }

    @Test
    @DisplayName("성공: isCompleted(), isCancelled() false 조건 검증")
    void is_status_check_failures() {
        Payment payment = Payment.of(1L, 0, 1000L);

        assertThat(payment.isCompleted()).isFalse();
        assertThat(payment.isCancelled()).isFalse();
    }

    @Test
    @DisplayName("of(): totalPrice가 음수이면 예외 발생 가능성 (비즈니스 로직 확장 시 필요)")
    void create_payment_with_negative_price() {
        Payment payment = Payment.of(1L, 1, -1000L);
        assertThat(payment.getTotalPrice()).isLessThan(0);
    }

    @Test
    @DisplayName("cancel() 이후 isCompleted()는 false 반환")
    void cancel_then_isCompleted_isFalse() {
        Payment payment = Payment.of(1L, 1, 3000L);
        payment.cancel();
        assertThat(payment.isCompleted()).isFalse();
    }

}
