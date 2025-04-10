package kr.hhplus.be.server.domain.product.service.dto;

import kr.hhplus.be.server.domain.product.dto.ProductListServiceDto;
import kr.hhplus.be.server.domain.product.dto.ProductServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ProductListServiceDtoTest {

    @Test
    @DisplayName("성공: 유효한 products 리스트로 생성 성공")
    void create_success() {
        // given
        ProductServiceResponse response = new ProductServiceResponse(
                1L, "상품명", 1000L, 1, "2025-04-10T10:00:00", List.of()
        );

        // when
        ProductListServiceDto dto = new ProductListServiceDto(List.of(response));

        // then
        assertThat(dto.products()).hasSize(1);
        assertThat(dto.products().get(0).id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패: products가 null이면 예외 발생")
    void create_fail_when_null() {
        // when & then
        assertThatThrownBy(() -> new ProductListServiceDto(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("products는 null일 수 없습니다.");
    }

    @Test
    @DisplayName("성공: 빈 리스트는 허용됨")
    void create_success_when_empty_list() {
        // when
        ProductListServiceDto dto = new ProductListServiceDto(List.of());

        // then
        assertThat(dto.products()).isEmpty();
    }
}
