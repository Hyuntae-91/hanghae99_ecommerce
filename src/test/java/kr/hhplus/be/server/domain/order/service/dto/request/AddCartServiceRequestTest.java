package kr.hhplus.be.server.domain.order.service.dto.request;


import kr.hhplus.be.server.domain.order.dto.request.AddCartServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AddCartServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 값으로 AddCartServiceRequest 생성")
    void validRequest_doesNotThrow() {
        assertThatCode(() -> new AddCartServiceRequest(1L, 1L, 1L, 1000L, 2))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: userId가 null이면 예외 발생")
    void nullUserId_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(null, 1L, 1L, 1000L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만이면 예외 발생")
    void invalidUserId_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(0L, 1L, 1L, 1000L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: productId가 null이면 예외 발생")
    void nullProductId_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(1L, null, 1L, 1000L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: productId가 1 미만이면 예외 발생")
    void invalidProductId_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(1L, 0L, 1L, 1000L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: optionId가 null이면 예외 발생")
    void nullOptionId_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(1L, 1L, null, 1000L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optionId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: optionId가 1 미만이면 예외 발생")
    void invalidOptionId_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(1L, 1L, 0L, 1000L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optionId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: eachPrice가 null이면 예외 발생")
    void nullEachPrice_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(1L, 1L, 1L, null, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eachPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: eachPrice가 0 미만이면 예외 발생")
    void invalidEachPrice_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(1L, 1L, 1L, -1000L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eachPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: quantity가 null이면 예외 발생")
    void nullQuantity_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(1L, 1L, 1L, 1000L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: quantity가 1 미만이면 예외 발생")
    void invalidQuantity_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceRequest(1L, 1L, 1L, 1000L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity는 1 이상이어야 합니다.");
    }
}
