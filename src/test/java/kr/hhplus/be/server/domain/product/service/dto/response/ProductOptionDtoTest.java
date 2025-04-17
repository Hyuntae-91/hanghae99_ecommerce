package kr.hhplus.be.server.domain.product.service.dto.response;

import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.interfaces.api.product.dto.response.ProductOptionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductOptionDtoTest {

    @Test
    @DisplayName("성공: 유효한 값을 가진 ProductOptionDto 생성")
    void validProductOptionDto() {
        // given
        Long optionId = 1L;
        int size = 10;
        int stock = 5;

        // when
        ProductOptionDto dto = new ProductOptionDto(optionId, size, stock);

        // then
        assertThat(dto.optionId()).isEqualTo(optionId);
        assertThat(dto.size()).isEqualTo(size);
        assertThat(dto.stock()).isEqualTo(stock);
    }

    @Test
    @DisplayName("실패: optionId가 1보다 작은 경우 예외 발생")
    void invalidOptionId() {
        // given
        Long optionId = 0L; // Invalid optionId
        int size = 10;
        int stock = 5;

        // then
        assertThatThrownBy(() -> new ProductOptionDto(optionId, size, stock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optionId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: size가 음수일 경우 예외 발생")
    void invalidSize() {
        // given
        Long optionId = 1L;
        int size = -5; // Invalid size
        int stock = 5;

        // then
        assertThatThrownBy(() -> new ProductOptionDto(optionId, size, stock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: stock이 음수일 경우 예외 발생")
    void invalidStock() {
        // given
        Long optionId = 1L;
        int size = 10;
        int stock = -3; // Invalid stock

        // then
        assertThatThrownBy(() -> new ProductOptionDto(optionId, size, stock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("성공: from 메소드로 ProductOptionResponse에서 DTO 변환")
    void fromMethodTest() {
        // given
        ProductOptionResponse response = new ProductOptionResponse(1L, 10, 5);

        // when
        ProductOptionDto dto = ProductOptionDto.from(response);

        // then
        assertThat(dto.optionId()).isEqualTo(1L);
        assertThat(dto.size()).isEqualTo(10);
        assertThat(dto.stock()).isEqualTo(5);
    }
}
