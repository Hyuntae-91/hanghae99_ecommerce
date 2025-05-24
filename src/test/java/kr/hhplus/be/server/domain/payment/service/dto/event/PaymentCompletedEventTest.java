package kr.hhplus.be.server.domain.payment.service.dto.event;

import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentCompletedEventTest {

    @Test
    @DisplayName("성공: 모든 필드가 유효할 때 객체 생성")
    void create_success() {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                1L, 100L, 1, 5000L, "2024-01-01T00:00:00", List.of(10L, 20L)
        );

        assertThat(event.paymentId()).isEqualTo(1L);
        assertThat(event.orderId()).isEqualTo(100L);
        assertThat(event.status()).isEqualTo(1);
        assertThat(event.totalPrice()).isEqualTo(5000L);
        assertThat(event.createdAt()).isEqualTo("2024-01-01T00:00:00");
        assertThat(event.productIds()).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("실패: paymentId가 null이면 예외 발생")
    void fail_when_paymentId_null() {
        assertThatThrownBy(() ->
                new PaymentCompletedEvent(null, 100L, 1, 5000L, "2024-01-01", List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: paymentId가 1 미만이면 예외 발생")
    void fail_when_paymentId_invalid() {
        assertThatThrownBy(() ->
                new PaymentCompletedEvent(0L, 100L, 1, 5000L, "2024-01-01", List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 null이면 예외 발생")
    void fail_when_orderId_null() {
        assertThatThrownBy(() ->
                new PaymentCompletedEvent(1L, null, 1, 5000L, "2024-01-01", List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 1 미만이면 예외 발생")
    void fail_when_orderId_invalid() {
        assertThatThrownBy(() ->
                new PaymentCompletedEvent(1L, 0L, 1, 5000L, "2024-01-01", List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 null이면 예외 발생")
    void fail_when_totalPrice_null() {
        assertThatThrownBy(() ->
                new PaymentCompletedEvent(1L, 1L, 1, null, "2024-01-01", List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 음수이면 예외 발생")
    void fail_when_totalPrice_negative() {
        assertThatThrownBy(() ->
                new PaymentCompletedEvent(1L, 1L, 1, -100L, "2024-01-01", List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: productIds가 null이면 예외 발생")
    void fail_when_productIds_null() {
        assertThatThrownBy(() ->
                new PaymentCompletedEvent(1L, 1L, 1, 1000L, "2024-01-01", null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productIds는 null 또는 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: productIds가 비어 있으면 예외 발생")
    void fail_when_productIds_empty() {
        assertThatThrownBy(() ->
                new PaymentCompletedEvent(1L, 1L, 1, 1000L, "2024-01-01", List.of())
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productIds는 null 또는 비어 있을 수 없습니다.");
    }
}
