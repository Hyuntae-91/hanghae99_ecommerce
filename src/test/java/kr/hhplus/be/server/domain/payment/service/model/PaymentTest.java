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
    @DisplayName("of(): totalPrice가 음수이면 예외 발생 가능성 (비즈니스 로직 확장 시 필요)")
    void create_payment_with_negative_price() {
        Payment payment = Payment.of(1L, 1, -1000L);
        assertThat(payment.getTotalPrice()).isLessThan(0);
    }
}
