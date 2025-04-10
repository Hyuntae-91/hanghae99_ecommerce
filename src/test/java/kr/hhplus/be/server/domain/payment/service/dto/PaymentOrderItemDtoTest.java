package kr.hhplus.be.server.domain.payment.service.dto;

import kr.hhplus.be.server.domain.payment.dto.PaymentOrderItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentOrderItemDtoTest {

    @Test
    @DisplayName("성공: 유효한 orderItemId와 optionId로 생성")
    void create_valid_dto() {
        PaymentOrderItemDto dto = new PaymentOrderItemDto(1L, 2L);

        assertThat(dto.orderItemId()).isEqualTo(1L);
        assertThat(dto.optionId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("실패: orderItemId가 null일 경우")
    void fail_when_orderItemId_is_null() {
        assertThatThrownBy(() -> new PaymentOrderItemDto(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderItemId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderItemId가 1보다 작을 경우")
    void fail_when_orderItemId_less_than_1() {
        assertThatThrownBy(() -> new PaymentOrderItemDto(0L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderItemId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: optionId가 null일 경우")
    void fail_when_optionId_is_null() {
        assertThatThrownBy(() -> new PaymentOrderItemDto(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optionId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: optionId가 1보다 작을 경우")
    void fail_when_optionId_less_than_1() {
        assertThatThrownBy(() -> new PaymentOrderItemDto(1L, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optionId는 1 이상이어야 합니다.");
    }
}
