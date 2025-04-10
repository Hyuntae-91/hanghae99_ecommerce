package kr.hhplus.be.server.domain.order.service.dto;


import kr.hhplus.be.server.domain.order.dto.CreateOrderServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CreateOrderServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 값으로 생성")
    void create_valid_request() {
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 0L, 1000L);

        assertThat(request.userId()).isEqualTo(1L);
        assertThat(request.couponIssueId()).isEqualTo(0L);
        assertThat(request.totalPrice()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("실패: userId가 null")
    void create_fail_when_user_id_is_null() {
        assertThatThrownBy(() -> new CreateOrderServiceRequest(null, 0L, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1보다 작음")
    void create_fail_when_user_id_less_than_1() {
        assertThatThrownBy(() -> new CreateOrderServiceRequest(0L, 0L, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 null")
    void create_fail_when_total_price_is_null() {
        assertThatThrownBy(() -> new CreateOrderServiceRequest(1L, 0L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 음수")
    void create_fail_when_total_price_is_negative() {
        assertThatThrownBy(() -> new CreateOrderServiceRequest(1L, 0L, -100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("totalPrice는 0 이상이어야 합니다.");
    }
}
