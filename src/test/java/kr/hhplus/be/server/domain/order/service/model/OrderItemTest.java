package kr.hhplus.be.server.domain.order.service.model;

import kr.hhplus.be.server.domain.order.dto.response.CartItemResponse;
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
        orderItem = OrderItem.of(1L, 1L, 1L, 1000L, 2);
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
    @DisplayName("성공: CartItemResponse 생성")
    void toCartItemResponse() {
        // given
        orderOption = mock(OrderOption.class);  // Mocking OrderOption
        when(orderOption.getId()).thenReturn(1L);
        when(orderOption.getStockQuantity()).thenReturn(10);
        when(orderOption.getSize()).thenReturn(100);

        OrderItem orderItem = OrderItem.of(1L, 1L, 1L, 1000L, 2);

        // Set the orderOption for the OrderItem
        CartItemResponse cartItemResponse = orderItem.toCartItemResponse(orderOption);

        // when
        assertThat(cartItemResponse.productId()).isEqualTo(1L);
        assertThat(cartItemResponse.quantity()).isEqualTo(2);
        assertThat(cartItemResponse.optionId()).isEqualTo(1L);
        assertThat(cartItemResponse.eachPrice()).isEqualTo(1000L);
        assertThat(cartItemResponse.stockQuantity()).isEqualTo(10);
        assertThat(cartItemResponse.size()).isEqualTo(100);
    }

}
