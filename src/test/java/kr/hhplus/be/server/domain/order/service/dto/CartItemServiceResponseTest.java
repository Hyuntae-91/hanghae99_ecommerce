package kr.hhplus.be.server.domain.order.service.dto;

import kr.hhplus.be.server.domain.order.dto.CartItemResponse;
import kr.hhplus.be.server.domain.order.dto.CartItemServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartItemServiceResponseTest {

    @Test
    @DisplayName("성공: CartItemServiceResponse 정상 생성")
    void create_success() {
        // given
        CartItemResponse item = new CartItemResponse(1L, 2, 10L, 1000L, 50, 270);
        List<CartItemResponse> cartList = List.of(item);
        Long totalPrice = 1000L;

        // when
        CartItemServiceResponse response = new CartItemServiceResponse(cartList, totalPrice);

        // then
        assertNotNull(response);
        assertEquals(1, response.cartList().size());
        assertEquals(1000L, response.totalPrice());
    }

    @Test
    @DisplayName("실패: cartList가 null일 경우 예외 발생")
    void create_fail_cartList_null() {
        // given
        List<CartItemResponse> cartList = null;
        Long totalPrice = 1000L;

        // then
        assertThrows(IllegalArgumentException.class, () -> new CartItemServiceResponse(cartList, totalPrice));
    }

    @Test
    @DisplayName("실패: cartList가 비어있을 경우 예외 발생")
    void create_fail_cartList_empty() {
        // given
        List<CartItemResponse> cartList = List.of();
        Long totalPrice = 1000L;

        // then
        assertThrows(IllegalArgumentException.class, () -> new CartItemServiceResponse(cartList, totalPrice));
    }

    @Test
    @DisplayName("실패: totalPrice가 null일 경우 예외 발생")
    void create_fail_totalPrice_null() {
        // given
        CartItemResponse item = new CartItemResponse(1L, 2, 10L, 1000L, 50, 270);
        List<CartItemResponse> cartList = List.of(item);
        Long totalPrice = null;

        // then
        assertThrows(IllegalArgumentException.class, () -> new CartItemServiceResponse(cartList, totalPrice));
    }

    @Test
    @DisplayName("실패: totalPrice가 음수일 경우 예외 발생")
    void create_fail_totalPrice_negative() {
        // given
        CartItemResponse item = new CartItemResponse(1L, 2, 10L, 1000L, 50, 270);
        List<CartItemResponse> cartList = List.of(item);
        Long totalPrice = -100L;

        // then
        assertThrows(IllegalArgumentException.class, () -> new CartItemServiceResponse(cartList, totalPrice));
    }
}
