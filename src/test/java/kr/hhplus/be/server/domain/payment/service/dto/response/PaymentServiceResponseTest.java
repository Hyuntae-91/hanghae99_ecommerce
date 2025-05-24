package kr.hhplus.be.server.domain.payment.service.dto.response;

import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentServiceResponseTest {

    @Test
    @DisplayName("성공: 모든 값이 유효하면 생성된다")
    void create_success() {
        PaymentServiceResponse response = new PaymentServiceResponse(
                1L, 100L, 1, 5000L, "2024-01-01T12:00:00"
        );

        assertThat(response.paymentId()).isEqualTo(1L);
        assertThat(response.orderId()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(1);
        assertThat(response.totalPrice()).isEqualTo(5000L);
        assertThat(response.createdAt()).isEqualTo("2024-01-01T12:00:00");
    }

    @Test
    @DisplayName("실패: paymentId가 null이면 예외 발생")
    void fail_when_paymentId_null() {
        assertThatThrownBy(() ->
                new PaymentServiceResponse(null, 100L, 1, 1000L, "2024-01-01")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: paymentId가 1 미만이면 예외 발생")
    void fail_when_paymentId_invalid() {
        assertThatThrownBy(() ->
                new PaymentServiceResponse(0L, 100L, 1, 1000L, "2024-01-01")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 null이면 예외 발생")
    void fail_when_orderId_null() {
        assertThatThrownBy(() ->
                new PaymentServiceResponse(1L, null, 1, 1000L, "2024-01-01")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 1 미만이면 예외 발생")
    void fail_when_orderId_invalid() {
        assertThatThrownBy(() ->
                new PaymentServiceResponse(1L, 0L, 1, 1000L, "2024-01-01")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 null이면 예외 발생")
    void fail_when_totalPrice_null() {
        assertThatThrownBy(() ->
                new PaymentServiceResponse(1L, 100L, 1, null, "2024-01-01")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 음수이면 예외 발생")
    void fail_when_totalPrice_negative() {
        assertThatThrownBy(() ->
                new PaymentServiceResponse(1L, 100L, 1, -100L, "2024-01-01")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: createdAt이 null이면 예외 발생")
    void fail_when_createdAt_null() {
        assertThatThrownBy(() ->
                new PaymentServiceResponse(1L, 100L, 1, 1000L, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("createdAt은 null이거나 공백일 수 없습니다.");
    }

    @Test
    @DisplayName("실패: createdAt이 공백이면 예외 발생")
    void fail_when_createdAt_blank() {
        assertThatThrownBy(() ->
                new PaymentServiceResponse(1L, 100L, 1, 1000L, " ")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("createdAt은 null이거나 공백일 수 없습니다.");
    }
}
