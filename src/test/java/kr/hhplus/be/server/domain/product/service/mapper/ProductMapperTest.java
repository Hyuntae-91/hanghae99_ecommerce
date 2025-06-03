package kr.hhplus.be.server.domain.product.service.mapper;

import kr.hhplus.be.server.domain.order.dto.event.OrderCreatedEvent;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.product.dto.event.ProductTotalPriceCompletedEvent;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.product.mapper.ProductMapper;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.interfaces.event.product.payload.OrderCreatedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductDataIds;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductTotalPriceCompletedPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    @DisplayName("성공: OrderOption → ProductOptionResponse 매핑")
    void toProductOptionResponse_success() {
        OrderOption orderOption = OrderOption.builder()
                .id(10L)
                .productId(100L)
                .size(275)
                .stockQuantity(30)
                .createdAt("2025-05-01T00:00:00")
                .updatedAt("2025-05-01T00:00:00")
                .build();

        ProductOptionResponse response = mapper.toProductOptionResponse(orderOption);

        assertThat(response.optionId()).isEqualTo(10L);
        assertThat(response.stock()).isEqualTo(30);
    }

    @Test
    @DisplayName("성공: ProductOptionKeyDto 리스트에서 중복 제거 후 productId 추출")
    void extractProductIds_success() {
        List<ProductOptionKeyDto> input = List.of(
                new ProductOptionKeyDto(1L, 10L, 100L),
                new ProductOptionKeyDto(2L, 20L, 200L),
                new ProductOptionKeyDto(1L, 30L, 300L) // 중복 productId
        );

        List<Long> result = mapper.extractProductIds(input);

        assertThat(result).containsExactlyInAnyOrder(1L, 2L); // 순서 무시하고 검증
    }

    @Test
    @DisplayName("성공: OrderCreatedPayload + Response → ProductTotalPriceCompletedEvent 변환")
    void toProductTotalPriceCompletedEvent_success() {
        List<ProductDataIds> items = List.of(
                new ProductDataIds(1L, 101L, 1001L, 1),
                new ProductDataIds(2L, 102L, 1002L, 1)
        );

        OrderCreatedPayload event = new OrderCreatedPayload(
                999L,
                888L,
                777L,
                1L,
                items
        );

        ProductTotalPriceResponse response = ProductTotalPriceResponse.from(5000L);

        ProductTotalPriceCompletedPayload completedEvent = mapper.toProductTotalPriceCompletedPayload(event, response);

        assertThat(completedEvent.orderId()).isEqualTo(999L);
        assertThat(completedEvent.userId()).isEqualTo(888L);
        assertThat(completedEvent.couponId()).isEqualTo(777L);
        assertThat(completedEvent.totalPrice()).isEqualTo(5000L);
        assertThat(completedEvent.items().isEmpty()).isFalse();
    }

    @Test
    @DisplayName("성공: null ProductOptionKeyDto 리스트 처리")
    void extractProductIds_null() {
        List<Long> ids = mapper.extractProductIds(null);
        assertThat(ids).isEmpty();
    }

    @Test
    @DisplayName("성공: Product → ProductServiceResponse 매핑 (옵션 무시)")
    void productToProductServiceResponse_success() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(1000L)
                .state(1)
                .createdAt("2025-05-01T00:00:00")
                .updatedAt("2025-05-01T00:00:00")
                .build();

        ProductServiceResponse response = mapper.productToProductServiceResponse(product);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Test Product");
        assertThat(response.price()).isEqualTo(1000L);
        assertThat(response.createdAt()).isEqualTo("2025-05-01T00:00:00");
    }
}
