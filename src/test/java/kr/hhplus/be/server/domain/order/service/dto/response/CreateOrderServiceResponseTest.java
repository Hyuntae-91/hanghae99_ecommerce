package kr.hhplus.be.server.domain.order.service.dto.response;

import kr.hhplus.be.server.domain.order.dto.response.CreateOrderServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CreateOrderServiceResponseTest {

    @Test
    @DisplayName("성공: 유효한 orderId로 생성")
    void create_success() {
        CreateOrderServiceResponse response = new CreateOrderServiceResponse(1L);

        assertThat(response.orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패: orderId가 null")
    void create_fail_null_order_id() {
        assertThatThrownBy(() -> new CreateOrderServiceResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 1 미만")
    void create_fail_order_id_less_than_1() {
        assertThatThrownBy(() -> new CreateOrderServiceResponse(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderId는 1 이상이어야 합니다.");
    }
}
