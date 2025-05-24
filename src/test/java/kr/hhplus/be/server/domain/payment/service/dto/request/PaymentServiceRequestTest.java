package kr.hhplus.be.server.domain.payment.service.dto.request;

import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 값으로 객체 생성")
    void create_valid_request() {
        PaymentServiceRequest request = new PaymentServiceRequest(1L, 1000L, 10L);

        assertThat(request.userId()).isEqualTo(1L);
        assertThat(request.totalPrice()).isEqualTo(1000L);
        assertThat(request.orderId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("실패: userId가 null인 경우")
    void fail_when_userId_is_null() {
        assertThatThrownBy(() -> new PaymentServiceRequest(null, 1000L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만인 경우")
    void fail_when_userId_less_than_1() {
        assertThatThrownBy(() -> new PaymentServiceRequest(0L, 1000L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 null인 경우")
    void fail_when_totalPrice_is_null() {
        assertThatThrownBy(() -> new PaymentServiceRequest(1L, null, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 0 미만인 경우")
    void fail_when_totalPrice_less_than_0() {
        assertThatThrownBy(() -> new PaymentServiceRequest(1L, -500L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 null인 경우")
    void fail_when_orderId_is_null() {
        assertThatThrownBy(() -> new PaymentServiceRequest(1L, 1000L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 0 미만인 경우")
    void fail_when_orderId_less_than_0() {
        assertThatThrownBy(() -> new PaymentServiceRequest(1L, 1000L, -1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 0 이상이어야 합니다.");
    }
}
