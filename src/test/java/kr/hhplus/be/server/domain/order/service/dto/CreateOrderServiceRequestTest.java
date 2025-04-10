package kr.hhplus.be.server.domain.order.service.dto;

import kr.hhplus.be.server.domain.order.dto.CreateOrderItemDto;
import kr.hhplus.be.server.domain.order.dto.CreateOrderServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CreateOrderServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 값으로 생성")
    void create_valid_request() {
        List<CreateOrderItemDto> items = List.of(new CreateOrderItemDto(1L, 2));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 0L, items);

        assertThat(request.userId()).isEqualTo(1L);
        assertThat(request.couponIssueId()).isEqualTo(0L);
        assertThat(request.items()).hasSize(1);
    }

    @Test
    @DisplayName("실패: userId가 null")
    void create_fail_when_user_id_is_null() {
        List<CreateOrderItemDto> items = List.of(new CreateOrderItemDto(1L, 2));
        assertThatThrownBy(() -> new CreateOrderServiceRequest(null, 0L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1보다 작음")
    void create_fail_when_user_id_less_than_1() {
        List<CreateOrderItemDto> items = List.of(new CreateOrderItemDto(1L, 2));
        assertThatThrownBy(() -> new CreateOrderServiceRequest(0L, 0L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: items가 null")
    void create_fail_when_items_is_null() {
        assertThatThrownBy(() -> new CreateOrderServiceRequest(1L, 0L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items는 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: items가 비어있음")
    void create_fail_when_items_is_empty() {
        assertThatThrownBy(() -> new CreateOrderServiceRequest(1L, 0L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items는 비어 있을 수 없습니다.");
    }
}
