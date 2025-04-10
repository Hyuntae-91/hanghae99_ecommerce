package kr.hhplus.be.server.domain.product.service.dto;

import kr.hhplus.be.server.domain.product.dto.ProductOptionKeyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductOptionKeyDtoTest {

    @Test
    @DisplayName("성공: 유효한 productId와 optionId로 생성된다")
    void create_success() {
        // when
        ProductOptionKeyDto dto = new ProductOptionKeyDto(1L, 2L, 1L);

        // then
        assertThat(dto.productId()).isEqualTo(1L);
        assertThat(dto.optionId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("실패: productId가 null이면 예외 발생")
    void fail_when_productId_is_null() {
        assertThatThrownBy(() -> new ProductOptionKeyDto(null, 2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("productId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: productId가 1 미만이면 예외 발생")
    void fail_when_productId_less_than_1() {
        assertThatThrownBy(() -> new ProductOptionKeyDto(0L, 2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("productId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: optionId가 null이면 예외 발생")
    void fail_when_optionId_is_null() {
        assertThatThrownBy(() -> new ProductOptionKeyDto(1L, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("optionId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: optionId가 1 미만이면 예외 발생")
    void fail_when_optionId_less_than_1() {
        assertThatThrownBy(() -> new ProductOptionKeyDto(1L, 0L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("optionId는 1 이상이어야 합니다.");
    }
}
