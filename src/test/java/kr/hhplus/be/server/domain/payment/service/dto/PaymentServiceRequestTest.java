package kr.hhplus.be.server.domain.payment.service.dto;


import kr.hhplus.be.server.domain.payment.dto.request.PaymentOrderItemDto;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PaymentServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 값으로 객체 생성")
    void create_valid_request() {
        PaymentOrderItemDto item = new PaymentOrderItemDto(1L, 1L,2L, 1);
        PaymentServiceRequest request = new PaymentServiceRequest(1L, 1000L, 1L, List.of(item));

        assertThat(request.userId()).isEqualTo(1L);
        assertThat(request.totalPrice()).isEqualTo(1000L);
        assertThat(request.orderItems()).hasSize(1);
    }

    @Test
    @DisplayName("실패: userId가 null인 경우")
    void fail_when_userId_is_null() {
        PaymentOrderItemDto item = new PaymentOrderItemDto(1L, 1L,2L, 1);
        assertThatThrownBy(() -> new PaymentServiceRequest(null, 1000L,0L, List.of(item)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만인 경우")
    void fail_when_userId_less_than_1() {
        PaymentOrderItemDto item = new PaymentOrderItemDto(1L, 1L,2L, 1);
        assertThatThrownBy(() -> new PaymentServiceRequest(0L, 1000L, 0L, List.of(item)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 null인 경우")
    void fail_when_totalPrice_is_null() {
        PaymentOrderItemDto item = new PaymentOrderItemDto(1L, 1L, 2L, 1);
        assertThatThrownBy(() -> new PaymentServiceRequest(1L, null, 0L, List.of(item)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 0 미만인 경우")
    void fail_when_totalPrice_less_than_0() {
        PaymentOrderItemDto item = new PaymentOrderItemDto(1L, 1L,  2L, 1);
        assertThatThrownBy(() -> new PaymentServiceRequest(1L, -500L, 0L, List.of(item)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderItems가 null인 경우")
    void fail_when_orderItems_is_null() {
        assertThatThrownBy(() -> new PaymentServiceRequest(1L, 1000L, 1L, null))  // couponIssueId == 1L로 수정
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderItems는 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: orderItems가 빈 리스트인 경우")
    void fail_when_orderItems_is_empty() {
        assertThatThrownBy(() -> new PaymentServiceRequest(1L, 1000L, 1L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderItems는 null이거나 비어 있을 수 없습니다.");
    }
}
