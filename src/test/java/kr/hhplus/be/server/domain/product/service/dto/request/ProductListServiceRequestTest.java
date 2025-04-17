package kr.hhplus.be.server.domain.product.service.dto.request;

import kr.hhplus.be.server.domain.product.dto.request.ProductListServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductListServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 입력값으로 생성")
    void validInput_doesNotThrow() {
        assertThatCode(() -> new ProductListServiceRequest(1, 10, "createdAt"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: page가 1 미만이면 예외 발생")
    void pageLessThanOne_throwsException() {
        assertThatThrownBy(() -> new ProductListServiceRequest(0, 10, "createdAt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: size가 1 미만이면 예외 발생")
    void sizeLessThanOne_throwsException() {
        assertThatThrownBy(() -> new ProductListServiceRequest(1, 0, "createdAt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: sort가 null이면 예외 발생")
    void sortIsNull_throwsException() {
        assertThatThrownBy(() -> new ProductListServiceRequest(1, 10, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: sort가 공백이면 예외 발생")
    void sortIsBlank_throwsException() {
        assertThatThrownBy(() -> new ProductListServiceRequest(1, 10, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: sort가 null이면 예외 발생")
    void nullSort_throwsException() {
        assertThatThrownBy(() -> new ProductListServiceRequest(1, 10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sort 필드는 null이거나 빈 값이 될 수 없습니다.");
    }

    @Test
    @DisplayName("실패: sort가 빈 문자열이면 예외 발생")
    void blankSort_throwsException() {
        assertThatThrownBy(() -> new ProductListServiceRequest(1, 10, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sort 필드는 null이거나 빈 값이 될 수 없습니다.");
    }
}
