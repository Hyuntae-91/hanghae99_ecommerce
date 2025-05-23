package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.product.assembler.ProductAssembler;
import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.ProductStates;
import kr.hhplus.be.server.domain.product.dto.request.ProductListServiceRequest;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.domain.product.dto.request.ProductServiceRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private OrderItemRepository orderItemRepository;
    private OrderOptionRepository orderOptionRepository;
    private ProductService productService;
    private ProductAssembler productAssembler;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        orderOptionRepository = mock(OrderOptionRepository.class);
        productAssembler = mock(ProductAssembler.class);

        productService = new ProductService(
                productRepository,
                orderItemRepository,
                orderOptionRepository,
                productAssembler
        );
    }

    @Test
    @DisplayName("성공: 상품 단건 조회")
    void get_product_by_id_success() {
        // given
        Product product = Product.builder()
                .id(1L)
                .name("테스트상품")
                .price(1000L)
                .state(1)
                .createdAt("2025-04-01 12:00:00")
                .build();

        OrderOption option = OrderOption.builder()
                .id(101L)
                .productId(1L)
                .size(275)
                .stockQuantity(10)
                .build();

        ProductOptionResponse optionResponse = new ProductOptionResponse(101L, 275, 10);

        ProductServiceResponse assembledResponse = new ProductServiceResponse(
                1L,
                "테스트상품",
                1000L,
                1,
                "2025-04-01 12:00:00",
                List.of(optionResponse)
        );

        when(productRepository.findById(1L)).thenReturn(product);
        when(orderOptionRepository.findByProductId(1L)).thenReturn(List.of(option));
        when(productAssembler.toResponseWithOptions(product, List.of(option)))
                .thenReturn(assembledResponse);

        // when
        var dto = productService.getProductById(new ProductServiceRequest(1L));

        // then
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("테스트상품");
        assertThat(dto.price()).isEqualTo(1000L);
        assertThat(dto.createdAt()).isEqualTo("2025-04-01 12:00:00");
        assertThat(dto.options()).hasSize(1);
        assertThat(dto.options().get(0).optionId()).isEqualTo(101L);
        assertThat(dto.options().get(0).size()).isEqualTo(275);
        assertThat(dto.options().get(0).stock()).isEqualTo(10);
    }

    @Test
    @DisplayName("성공: 상품 리스트 조회 (옵션 포함)")
    void get_product_list_success() {
        // given
        Product product1 = Product.builder()
                .id(1L)
                .name("상품1")
                .price(1000L)
                .state(1)
                .createdAt("2025-04-01 12:00:00")
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("상품2")
                .price(2000L)
                .state(1)
                .createdAt("2025-04-01 12:00:00")
                .build();

        List<Product> productList = List.of(product1, product2);

        List<Integer> excludedStates = List.of(
                ProductStates.DELETED.getCode(),
                ProductStates.SOLD_OUT.getCode()
        );

        when(productRepository.findByStateNotIn(eq(1), eq(10), eq("createdAt"), eq(excludedStates)))
                .thenReturn(productList);

        // order options
        OrderOption option1 = OrderOption.builder()
                .id(101L)
                .productId(1L)
                .size(275)
                .stockQuantity(10)
                .build();

        OrderOption option2 = OrderOption.builder()
                .id(102L)
                .productId(2L)
                .size(280)
                .stockQuantity(5)
                .build();

        when(orderOptionRepository.findByProductId(1L)).thenReturn(List.of(option1));
        when(orderOptionRepository.findByProductId(2L)).thenReturn(List.of(option2));

        // 조립된 결과 DTO
        ProductOptionResponse optResponse1 = new ProductOptionResponse(101L, 275, 10);
        ProductOptionResponse optResponse2 = new ProductOptionResponse(102L, 280, 5);

        ProductServiceResponse dto1 = new ProductServiceResponse(
                1L, "상품1", 1000L, 1, "2025-04-01 12:00:00", List.of(optResponse1)
        );
        ProductServiceResponse dto2 = new ProductServiceResponse(
                2L, "상품2", 2000L, 1, "2025-04-01 12:00:00", List.of(optResponse2)
        );

        // Assembler stub 설정
        when(productAssembler.toResponseWithOptions(product1, List.of(option1))).thenReturn(dto1);
        when(productAssembler.toResponseWithOptions(product2, List.of(option2))).thenReturn(dto2);

        // when
        var result = productService.getProductList(new ProductListServiceRequest(1, 10, "createdAt"));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("상품1");
        assertThat(result.get(0).options()).hasSize(1);
        assertThat(result.get(0).options().get(0).optionId()).isEqualTo(101L);
        assertThat(result.get(0).options().get(0).size()).isEqualTo(275);
        assertThat(result.get(0).options().get(0).stock()).isEqualTo(10);
    }

    @Test
    @DisplayName("성공: 인기 상품 총액 계산")
    void calculate_total_price_success() {
        // given
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 11L, 1L); // itemId: 1L
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(1L, 12L, 2L); // itemId: 2L
        List<ProductOptionKeyDto> request = List.of(item1, item2);

        // OrderItem mock
        OrderItem orderItem1 = OrderItem.of(1L, 1L, 11L, 100L, 1);
        OrderItem orderItem2 = OrderItem.of(1L, 1L, 12L, 300L, 1);
        orderItem1.setId(1L);
        orderItem2.setId(2L);

        when(orderItemRepository.findById(1L)).thenReturn(orderItem1);
        when(orderItemRepository.findById(2L)).thenReturn(orderItem2);

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(400L);
    }

    @Test
    @DisplayName("실패: 상품 단건 조회 - 존재하지 않는 ID")
    void get_product_by_id_fail_when_not_found() {
        // given
        when(productRepository.findById(99L)).thenThrow(new ResourceNotFoundException("Product Not Found"));

        // then
        assertThrows(ResourceNotFoundException.class, () ->
                productService.getProductById(new ProductServiceRequest(99L)));
    }

    @Test
    @DisplayName("실패: OrderItem이 존재하지 않으면 ResourceNotFoundException 발생")
    void calculateTotalPrice_when_orderItem_not_found_should_throw_exception() {
        // given
        ProductOptionKeyDto item = new ProductOptionKeyDto(1L, 1L, 1L); // itemId = 1
        List<ProductOptionKeyDto> request = List.of(item);

        when(orderItemRepository.findById(1L))
                .thenThrow(new ResourceNotFoundException("OrderItem not found: 1"));

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> productService.calculateTotalPrice(request));
    }


    @Test
    @DisplayName("성공: OrderItem이 없으면 총합은 0으로 계산된다")
    void calculateTotalPrice_when_no_orderItems_should_return_zero() {
        // given
        List<ProductOptionKeyDto> request = List.of();

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(0L);
    }

    @Test
    @DisplayName("실패: ProductOptionKeyDto - productId가 null이면 예외 발생")
    void productOptionKeyDto_fail_productId_null() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductOptionKeyDto(null, 1L, 1L));
    }

    @Test
    @DisplayName("실패: ProductOptionKeyDto - productId가 1 미만이면 예외 발생")
    void productOptionKeyDto_fail_productId_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductOptionKeyDto(0L, 1L, 1L));
    }

    @Test
    @DisplayName("실패: ProductOptionKeyDto - optionId가 null이면 예외 발생")
    void productOptionKeyDto_fail_optionId_null() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductOptionKeyDto(1L, null, 1L));
    }

    @Test
    @DisplayName("실패: ProductOptionKeyDto - optionId가 1 미만이면 예외 발생")
    void productOptionKeyDto_fail_optionId_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductOptionKeyDto(1L, 0L, 1L));
    }

    @Test
    @DisplayName("실패: ProductOptionKeyDto - quantity가 null이면 예외 발생")
    void productOptionKeyDto_fail_quantity_null() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductOptionKeyDto(1L, 1L, null));
    }

    @Test
    @DisplayName("실패: ProductOptionKeyDto - quantity가 1 미만이면 예외 발생")
    void productOptionKeyDto_fail_quantity_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductOptionKeyDto(1L, 1L, 0L));
    }

    @Test
    @DisplayName("성공: 요청된 itemId들에 대해서만 총합 계산")
    void calculateTotalPrice_with_selected_items_only() {
        // given
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 11L, 1L); // itemId = 1
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(1L, 12L, 2L); // itemId = 2
        List<ProductOptionKeyDto> request = List.of(item1, item2);

        OrderItem orderItem1 = OrderItem.of(1L, 1L, 11L, 100L, 1);
        OrderItem orderItem2 = OrderItem.of(1L, 1L, 12L, 300L, 1);

        orderItem1.setId(1L);
        orderItem2.setId(2L);

        when(orderItemRepository.findById(1L)).thenReturn(orderItem1);
        when(orderItemRepository.findById(2L)).thenReturn(orderItem2);

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(400L);
    }


    @Test
    @DisplayName("실패: 상품 리스트 조회 - 유효하지 않은 정렬 조건")
    void get_product_list_fail_invalid_sort() {
        // given
        List<Integer> excludedStates = List.of(
                ProductStates.DELETED.getCode(),
                ProductStates.SOLD_OUT.getCode()
        );

        when(productRepository.findByStateNotIn(1, 10, "invalidField", excludedStates))
                .thenThrow(new IllegalArgumentException("Invalid sort parameter"));

        // then
        assertThrows(IllegalArgumentException.class, () ->
                productService.getProductList(new ProductListServiceRequest(1, 10, "invalidField")));
    }

    @Test
    @DisplayName("성공: 총 금액 계산")
    void calculateTotalPrice_success() {
        // given
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 1L, 1L); // itemId: 1L
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(2L, 2L, 2L); // itemId: 2L
        List<ProductOptionKeyDto> request = List.of(item1, item2);

        // OrderItem(amount, quantity): 1000 * 2 = 2000, 2000 * 3 = 6000
        OrderItem orderItem1 = OrderItem.of(1L, 1L, 1L, 1000L, 2);
        OrderItem orderItem2 = OrderItem.of(1L, 2L, 2L, 2000L, 3);
        orderItem1.setId(1L);
        orderItem2.setId(2L);

        when(orderItemRepository.findById(1L)).thenReturn(orderItem1);
        when(orderItemRepository.findById(2L)).thenReturn(orderItem2);

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(2000 + 6000);
    }

}
