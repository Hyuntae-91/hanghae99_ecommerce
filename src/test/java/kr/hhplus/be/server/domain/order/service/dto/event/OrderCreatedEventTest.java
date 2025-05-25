package kr.hhplus.be.server.domain.order.service.dto.event;

import kr.hhplus.be.server.domain.order.dto.event.OrderCreatedEvent;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderCreatedEventTest {

    @Test
    @DisplayName("성공: 유효한 값으로 생성")
    void create_success() {
        List<ProductOptionKeyDto> items = List.of(new ProductOptionKeyDto(1L, 1L, 1L));
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 1L, 1L, items);

        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.userId()).isEqualTo(1L);
        assertThat(event.couponId()).isEqualTo(1L);
        assertThat(event.items()).hasSize(1);
    }

    @Test
    @DisplayName("실패: orderId가 null이거나 1보다 작음")
    void fail_when_orderId_invalid() {
        List<ProductOptionKeyDto> items = List.of(new ProductOptionKeyDto(1L, 1L, 1L));

        assertThatThrownBy(() -> new OrderCreatedEvent(null, 1L, 1L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderId는 1 이상이어야 합니다.");

        assertThatThrownBy(() -> new OrderCreatedEvent(0L, 1L, 1L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 null이거나 1보다 작음")
    void fail_when_userId_invalid() {
        List<ProductOptionKeyDto> items = List.of(new ProductOptionKeyDto(1L, 1L, 1L));

        assertThatThrownBy(() -> new OrderCreatedEvent(1L, null, 1L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId는 1 이상이어야 합니다.");

        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 0L, 1L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: couponId가 0 이하")
    void fail_when_couponId_invalid() {
        List<ProductOptionKeyDto> items = List.of(new ProductOptionKeyDto(1L, 1L, 1L));

        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 1L, 0L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("couponId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("성공: couponId가 null인 경우 (쿠폰 없음)")
    void success_when_couponId_is_null() {
        List<ProductOptionKeyDto> items = List.of(new ProductOptionKeyDto(1L, 1L, 1L));

        OrderCreatedEvent event = new OrderCreatedEvent(1L, 1L, null, items);
        assertThat(event.couponId()).isNull();
    }

    @Test
    @DisplayName("실패: items가 null 또는 비어있음")
    void fail_when_items_invalid() {
        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 1L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items는 null이거나 비어 있을 수 없습니다.");

        assertThatThrownBy(() -> new OrderCreatedEvent(1L, 1L, 1L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items는 null이거나 비어 있을 수 없습니다.");
    }
}
