package kr.hhplus.be.server.domain.order.service.dto;

import kr.hhplus.be.server.domain.order.dto.response.AddCartServiceResponse;
import kr.hhplus.be.server.domain.order.dto.response.CartItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

class AddCartServiceResponseTest {

    @Test
    @DisplayName("성공: 유효한 값으로 AddCartServiceResponse 생성")
    void validResponse_doesNotThrow() {
        List<CartItemResponse> cartList = List.of(
                new CartItemResponse(1L, 1, 1L, 1000L, 1000, 10)
        );

        assertThatCode(() -> new AddCartServiceResponse(cartList, 1000L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: cartList가 null이면 예외 발생")
    void nullCartList_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceResponse(null, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cartList는 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: cartList가 비어 있으면 예외 발생")
    void emptyCartList_throwsException() {
        assertThatThrownBy(() -> new AddCartServiceResponse(List.of(), 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cartList는 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 null이면 예외 발생")
    void nullTotalPrice_throwsException() {
        List<CartItemResponse> cartList = List.of(
                new CartItemResponse(1L, 1, 1L, 1000L, 1000, 10)
        );

        assertThatThrownBy(() -> new AddCartServiceResponse(cartList, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: totalPrice가 음수이면 예외 발생")
    void negativeTotalPrice_throwsException() {
        List<CartItemResponse> cartList = List.of(
                new CartItemResponse(1L, 1, 1L, 1000L, 1000, 10)
        );

        assertThatThrownBy(() -> new AddCartServiceResponse(cartList, -1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPrice는 0 이상이어야 합니다.");
    }
}
