package kr.hhplus.be.server.domain.order.service.dto.response;

import kr.hhplus.be.server.domain.order.dto.response.CartItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CartItemResponseTest {

    @Test
    @DisplayName("성공: 유효한 값으로 CartItemResponse 생성")
    void validInput_doesNotThrow() {
        assertThatCode(() -> new CartItemResponse(1L, 1, 1L, 1000L, 10, 100))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: productId가 null이면 예외 발생")
    void nullProductId_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(null, 1, 1L, 1000L, 10, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: productId가 1 미만이면 예외 발생")
    void invalidProductId_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(0L, 1, 1L, 1000L, 10, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: optionId가 null이면 예외 발생")
    void nullOptionId_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(1L, 1, null, 1000L, 10, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optionId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: optionId가 1 미만이면 예외 발생")
    void invalidOptionId_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(1L, 1, 0L, 1000L, 10, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optionId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: quantity가 1 미만이면 예외 발생")
    void invalidQuantity_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(1L, 0, 1L, 1000L, 10, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: eachPrice가 null이면 예외 발생")
    void nullEachPrice_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(1L, 1, 1L, null, 10, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eachPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: eachPrice가 음수이면 예외 발생")
    void negativeEachPrice_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(1L, 1, 1L, -1000L, 10, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eachPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: stockQuantity가 음수이면 예외 발생")
    void negativeStockQuantity_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(1L, 1, 1L, 1000L, -1, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stockQuantity는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: size가 음수이면 예외 발생")
    void negativeSize_throwsException() {
        assertThatThrownBy(() -> new CartItemResponse(1L, 1, 1L, 1000L, 10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("성공: from() 메서드를 통한 CartItemResponse 복사")
    void fromMethod_doesNotThrow() {
        CartItemResponse original = new CartItemResponse(1L, 1, 1L, 1000L, 1000, 10);
        CartItemResponse copy = CartItemResponse.from(original);

        assertThat(copy).isEqualTo(original);
    }
}
