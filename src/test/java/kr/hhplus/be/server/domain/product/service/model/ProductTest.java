package kr.hhplus.be.server.domain.product.service.model;

import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.product.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductTest {

    private Product createProductWithState(int state) {
        return Product.builder()
                .id(1L)
                .name("테스트 상품")
                .price(1000L)
                .state(state)
                .createdAt("2024-04-01 12:00:00")
                .updatedAt("2024-04-01 12:00:00")
                .build();
    }

    @Test
    @DisplayName("상품 상태가 삭제 상태인 경우 isDeleted()는 true를 반환한다")
    void isDeletedTest() {
        Product product = createProductWithState(-1);
        assertThat(product.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("상품 상태가 판매중인 경우 isOnSale()은 true를 반환한다")
    void isOnSaleTest() {
        Product product = createProductWithState(1);
        assertThat(product.isOnSale()).isTrue();
    }

    @Test
    @DisplayName("상품 상태가 품절인 경우 isSoldOut()은 true를 반환한다")
    void isSoldOutTest() {
        Product product = createProductWithState(2);
        assertThat(product.isSoldOut()).isTrue();
    }

    @Test
    @DisplayName("상품 상태가 숨김 상태인 경우 isHidden()은 true를 반환한다")
    void isHiddenTest() {
        Product product = createProductWithState(3);
        assertThat(product.isHidden()).isTrue();
    }

    @Test
    @DisplayName("상품 상태가 null이면 모든 상태 확인 메서드는 false를 반환한다")
    void nullStateTest() {
        Product product = createProductWithState(0);
        assertThat(product.isDeleted()).isFalse();
        assertThat(product.isOnSale()).isFalse();
        assertThat(product.isSoldOut()).isFalse();
        assertThat(product.isHidden()).isFalse();
    }

    @Test
    @DisplayName("calculateTotalPrice: OrderItem들의 가격을 합산한다.")
    void calculateTotalPrice_success() {
        // given
        Product product = createProductWithState(1);

        // mock OrderItem
        OrderItem orderItem1 = mock(OrderItem.class);
        when(orderItem1.calculateTotalPrice()).thenReturn(1000L);

        OrderItem orderItem2 = mock(OrderItem.class);
        when(orderItem2.calculateTotalPrice()).thenReturn(2000L);

        product.setOrderItems(List.of(orderItem1, orderItem2));

        // when
        long totalPrice = product.getOrderItems().stream()
                .mapToLong(OrderItem::calculateTotalPrice)
                .sum();

        // then
        assertThat(totalPrice).isEqualTo(3000L);  // 1000L + 2000L
    }

    @Test
    @DisplayName("calculateTotalPrice: 상품에 주문 항목이 없으면 0을 반환한다.")
    void calculateTotalPrice_emptyOrderItems() {
        // given
        Product product = createProductWithState(1);

        // 주문 항목 없음
        product.setOrderItems(List.of());

        // when
        long totalPrice = product.getOrderItems().stream()
                .mapToLong(OrderItem::calculateTotalPrice)
                .sum();

        // then
        assertThat(totalPrice).isEqualTo(0L);
    }

    @Test
    @DisplayName("calculateTotalPrice: 옵션이 없는 주문 항목을 처리할 때 예외가 발생한다.")
    void calculateTotalPrice_noOption_throwsException() {
        // given
        Product product = createProductWithState(1);

        // mock OrderItem
        OrderItem orderItem = mock(OrderItem.class);
        // 옵션이 없어서 getOption 호출 시 예외 발생
        when(orderItem.getOption(any())).thenThrow(new IllegalStateException("옵션 정보를 찾을 수 없습니다."));

        product.setOrderItems(List.of(orderItem));

        // when / then
        assertThatThrownBy(() -> orderItem.getOption(product.getOrderOptions()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("옵션 정보를 찾을 수 없습니다.");
    }


}

