package kr.hhplus.be.server.domain.order.service.dto.request;

import kr.hhplus.be.server.domain.order.dto.request.CreateOrderOptionDto;
import kr.hhplus.be.server.domain.order.dto.request.CreateOrderServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CreateOrderServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 값으로 생성")
    void create_valid_request() {
        List<CreateOrderOptionDto> options = List.of(new CreateOrderOptionDto(1L, 2));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 0L, options);

        assertThat(request.userId()).isEqualTo(1L);
        assertThat(request.options()).hasSize(1);
    }

    @Test
    @DisplayName("실패: userId가 null")
    void create_fail_when_user_id_is_null() {
        List<CreateOrderOptionDto> options = List.of(new CreateOrderOptionDto(1L, 2));
        assertThatThrownBy(() -> new CreateOrderServiceRequest(null, 0L, options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1보다 작음")
    void create_fail_when_user_id_less_than_1() {
        List<CreateOrderOptionDto> options = List.of(new CreateOrderOptionDto(1L, 2));
        assertThatThrownBy(() -> new CreateOrderServiceRequest(0L, 0L, options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: options가 null")
    void create_fail_when_options_is_null() {
        assertThatThrownBy(() -> new CreateOrderServiceRequest(1L, 0L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orders 는 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: options가 비어있음")
    void create_fail_when_options_is_empty() {
        assertThatThrownBy(() -> new CreateOrderServiceRequest(1L, 0L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orders 는 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("성공: extractOptionIds() 메서드 동작 확인")
    void extract_option_ids_success() {
        List<CreateOrderOptionDto> options = List.of(
                new CreateOrderOptionDto(1L, 2),
                new CreateOrderOptionDto(2L, 3)
        );
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 0L, options);

        List<Long> result = request.extractOptionIds();

        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("성공: toQuantityMap() 메서드 동작 확인")
    void to_quantity_map_success() {
        List<CreateOrderOptionDto> options = List.of(
                new CreateOrderOptionDto(1L, 2),
                new CreateOrderOptionDto(2L, 3)
        );
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 0L, options);

        var map = request.toQuantityMap();

        assertThat(map).hasSize(2);
        assertThat(map.get(1L)).isEqualTo(2);
        assertThat(map.get(2L)).isEqualTo(3);
    }
}
