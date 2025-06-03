package kr.hhplus.be.server.domain.product.service.dto.event;

import kr.hhplus.be.server.domain.product.dto.event.ProductTotalPriceCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTotalPriceCompletedEventTest {

    @Test
    @DisplayName("성공: 정상 생성")
    void create_success() {
        ProductTotalPriceCompletedEvent event = new ProductTotalPriceCompletedEvent(
                1L, 2L, 3L, 1L, 1000L, List.of(10L, 20L)
        );

        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.userId()).isEqualTo(2L);
        assertThat(event.couponId()).isEqualTo(3L);
        assertThat(event.totalPrice()).isEqualTo(1000L);
        assertThat(event.productIds()).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("성공: couponId가 null이어도 생성 가능")
    void create_success_with_null_couponId() {
        ProductTotalPriceCompletedEvent event = new ProductTotalPriceCompletedEvent(
                1L, 2L, null, 1L, 1000L, List.of(10L)
        );

        assertThat(event.couponId()).isNull();
    }

    @Test
    @DisplayName("실패: orderId가 null")
    void fail_if_orderId_null() {
        assertThatThrownBy(() ->
                new ProductTotalPriceCompletedEvent(null, 2L, 3L, 1L, 1000L, List.of(10L))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: userId가 null")
    void fail_if_userId_null() {
        assertThatThrownBy(() ->
                new ProductTotalPriceCompletedEvent(1L, null, 3L, 1L,1000L, List.of(10L))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: totalPrice가 null")
    void fail_if_totalPrice_null() {
        assertThatThrownBy(() ->
                new ProductTotalPriceCompletedEvent(1L, 2L, 3L, 1L,null, List.of(10L))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: productIds가 null")
    void fail_if_productIds_null() {
        assertThatThrownBy(() ->
                new ProductTotalPriceCompletedEvent(1L, 2L, 3L, 1L,1000L, null)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("실패: productIds가 비어있음")
    void fail_if_productIds_empty() {
        assertThatThrownBy(() ->
                new ProductTotalPriceCompletedEvent(1L, 2L, 3L, 1L,1000L, List.of())
        ).isInstanceOf(IllegalArgumentException.class);
    }
}