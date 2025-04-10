package kr.hhplus.be.server.domain.order.service.model;

import kr.hhplus.be.server.domain.order.dto.CartItemResponse;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderItemTest {

    private Product product;
    private OrderOption orderOption;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        // Mock product and order option
        product = mock(Product.class);
        orderOption = mock(OrderOption.class);

        // Create OrderItem with mock data
        orderItem = OrderItem.of(1L, 1L, 1L, 1L, 1000L, 2);
    }

    @Test
    @DisplayName("성공: 총 가격 계산")
    void calculateTotalPrice() {
        // given
        Long expectedTotalPrice = 2000L;

        // when
        Long totalPrice = orderItem.calculateTotalPrice();

        // then
        assertThat(totalPrice).isEqualTo(expectedTotalPrice);
    }

    @Test
    @DisplayName("성공: 옵션 조회")
    void getOption() {
        // given
        List<OrderOption> options = List.of(orderOption);
        when(orderOption.getId()).thenReturn(1L);

        // when
        OrderOption option = orderItem.getOption(options);

        // then
        assertThat(option).isEqualTo(orderOption);
    }

    @Test
    @DisplayName("실패: 옵션이 존재하지 않으면 예외 발생")
    void getOption_throwsException_whenOptionNotFound() {
        // given
        List<OrderOption> options = List.of(orderOption);
        when(orderOption.getId()).thenReturn(2L);  // non-matching optionId

        // when & then
        assertThatThrownBy(() -> orderItem.getOption(options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("옵션이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("성공: 재고 수량 조회")
    void getStock() {
        // given
        List<OrderOption> options = List.of(orderOption);
        when(orderOption.getId()).thenReturn(1L);
        when(orderOption.getStockQuantity()).thenReturn(10);

        // when
        int stock = orderItem.getStock(options);

        // then
        assertThat(stock).isEqualTo(10);
    }

    @Test
    @DisplayName("성공: 사이즈 조회")
    void getSize() {
        // given
        List<OrderOption> options = List.of(orderOption);
        when(orderOption.getId()).thenReturn(1L);
        when(orderOption.getSize()).thenReturn(100);

        // when
        int size = orderItem.getSize(options);

        // then
        assertThat(size).isEqualTo(100);
    }

    @Test
    @DisplayName("성공: CartItemResponse 생성")
    void toCartItemResponse() {
        // given
        orderOption = mock(OrderOption.class);  // Mocking OrderOption
        when(orderOption.getStockQuantity()).thenReturn(10);
        when(orderOption.getSize()).thenReturn(100);

        List<OrderOption> options = List.of(orderOption);
        OrderItem orderItem = OrderItem.of(1L, null, 1L, 1L, 1000L, 2); // Create OrderItem

        // Set the orderOption for the OrderItem
        orderItem.setOrderOption(orderOption);

        // when
        CartItemResponse cartItemResponse = orderItem.toCartItemResponse();

        // then
        assertThat(cartItemResponse.productId()).isEqualTo(1L);
        assertThat(cartItemResponse.quantity()).isEqualTo(2);
        assertThat(cartItemResponse.optionId()).isEqualTo(1L);
        assertThat(cartItemResponse.eachPrice()).isEqualTo(1000L);
        assertThat(cartItemResponse.stockQuantity()).isEqualTo(10);
        assertThat(cartItemResponse.size()).isEqualTo(100);
    }

}
