package kr.hhplus.be.server.domain.product.service.dto;

import kr.hhplus.be.server.domain.product.dto.request.ProductServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 productId로 생성")
    void validInput_doesNotThrow() {
        assertThatCode(() -> new ProductServiceRequest(1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: productId가 null이면 예외 발생")
    void nullProductId_throwsException() {
        assertThatThrownBy(() -> new ProductServiceRequest(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: productId가 1 미만이면 예외 발생")
    void productIdLessThanOne_throwsException() {
        assertThatThrownBy(() -> new ProductServiceRequest(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
