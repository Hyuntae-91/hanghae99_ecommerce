package kr.hhplus.be.server.domain.order.service.mapper;

import kr.hhplus.be.server.domain.order.dto.request.CreateOrderServiceRequest;
import kr.hhplus.be.server.domain.order.dto.request.UpdateOrderServiceRequest;
import kr.hhplus.be.server.domain.order.mapper.OrderMapper;
import kr.hhplus.be.server.domain.order.mapper.OrderMapperImpl;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.interfaces.api.payment.dto.request.PaymentProductDto;
import kr.hhplus.be.server.interfaces.api.payment.dto.request.PaymentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapperImpl();

    @Test
    @DisplayName("성공: PaymentRequest -> CreateOrderServiceRequest 매핑")
    void toServiceRequest_success() {
        PaymentProductDto product = new PaymentProductDto(1L, "item1", 2L, 3L, 5);
        PaymentRequest request = new PaymentRequest(List.of(product), 10L);
        CreateOrderServiceRequest result = mapper.toServiceRequest(100L, request);

        assertThat(result.userId()).isEqualTo(100L);
        assertThat(result.couponId()).isEqualTo(10L);
        assertThat(result.options()).hasSize(1);
        assertThat(result.options().get(0).optionId()).isEqualTo(3L);
        assertThat(result.options().get(0).quantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("성공: OrderItem 리스트 -> ProductOptionKeyDto 리스트 매핑")
    void toProductOptionKeyDtoList_success() throws Exception {
        // given
        OrderItem item = OrderItem.of(1L, 101L, 201L, 1000L, 2);
        item.applyOrderId(999L);

        Field idField = OrderItem.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(item, 1L); // itemId를 1 이상으로 설정

        // when
        List<ProductOptionKeyDto> result = mapper.toProductOptionKeyDtoList(List.of(item));

        // then
        assertThat(result).hasSize(1);
        ProductOptionKeyDto dto = result.get(0);
        assertThat(dto.productId()).isEqualTo(101L);
        assertThat(dto.optionId()).isEqualTo(201L);
        assertThat(dto.itemId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공: PaymentCompletedEvent -> UpdateOrderServiceRequest 매핑")
    void toUpdateOrderRequest_success() {
        PaymentCompletedEvent event = new PaymentCompletedEvent(1L, 2L, 1, 5000L, "2025-05-23T12:00:00", List.of(10L));
        UpdateOrderServiceRequest result = mapper.toUpdateOrderRequest(event);

        assertThat(result.orderId()).isEqualTo(2L);
        assertThat(result.totalPrice()).isEqualTo(5000L);
    }
}
