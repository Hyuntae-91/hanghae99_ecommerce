package kr.hhplus.be.server.domain.product.service.dto;

import kr.hhplus.be.server.domain.product.dto.ProductServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductServiceResponseTest {

    @Test
    @DisplayName("성공: 유효한 데이터로 생성")
    void validInput_doesNotThrow() {
        assertThatCode(() -> new ProductServiceResponse(1L, "상품", 1000L, 1, "2025-04-01"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: id가 null이면 예외 발생")
    void nullId_throwsException() {
        assertThatThrownBy(() -> new ProductServiceResponse(null, "상품", 1000L, 1, "2025-04-01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: name이 공백이면 예외 발생")
    void blankName_throwsException() {
        assertThatThrownBy(() -> new ProductServiceResponse(1L, "  ", 1000L, 1, "2025-04-01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: price가 null이면 예외 발생")
    void nullPrice_throwsException() {
        assertThatThrownBy(() -> new ProductServiceResponse(1L, "상품", null, 1, "2025-04-01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: price가 음수이면 예외 발생")
    void negativePrice_throwsException() {
        assertThatThrownBy(() -> new ProductServiceResponse(1L, "상품", -100L, 1, "2025-04-01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: createdAt이 공백이면 예외 발생")
    void blankCreatedAt_throwsException() {
        assertThatThrownBy(() -> new ProductServiceResponse(1L, "상품", 1000L, 1, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: createdAt이 null이면 예외 발생")
    void nullCreatedAt_throwsException() {
        assertThatThrownBy(() -> new ProductServiceResponse(1L, "상품", 1000L, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
