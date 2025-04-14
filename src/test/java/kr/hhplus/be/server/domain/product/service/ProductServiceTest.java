package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.common.exception.ResourceNotFoundException;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductStates;
import kr.hhplus.be.server.domain.product.dto.*;
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
    private ProductService productService;
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productMapper = mock(ProductMapper.class);
        productService = new ProductService(productRepository, productMapper);
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
                .orderOptions(List.of())
                .build();

        ProductServiceResponse response = new ProductServiceResponse(
                1L,
                "테스트상품",
                1000L,
                1,
                "2025-04-01 12:00:00",
                List.of() // options
        );

        when(productRepository.findById(1L)).thenReturn(product);
        when(productMapper.productToProductServiceResponse(product)).thenReturn(response);

        // when
        var dto = productService.getProductById(new ProductServiceRequest(1L));

        // then
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("테스트상품");
    }

    @Test
    @DisplayName("성공: 상품 리스트 조회")
    void get_product_list_success() {
        // given
        List<Product> productList = List.of(
                Product.builder().id(1L).name("상품1").price(1000L).state(1).orderOptions(List.of()).createdAt("2025-04-01 12:00:00").build(),
                Product.builder().id(2L).name("상품2").price(2000L).state(1).orderOptions(List.of()).createdAt("2025-04-01 12:00:00").build()
        );

        List<Integer> excludedStates = List.of(
                ProductStates.DELETED.getCode(),
                ProductStates.SOLD_OUT.getCode()
        );

        when(productRepository.findByStateNotIn(1, 10, "created_at", excludedStates)).thenReturn(productList);

        ProductServiceResponse dto1 = new ProductServiceResponse(1L, "상품1", 1000L, 1, "2025-04-01 12:00:00", List.of());
        ProductServiceResponse dto2 = new ProductServiceResponse(2L, "상품2", 2000L, 1, "2025-04-01 12:00:00", List.of());

        when(productMapper.productsToProductServiceResponses(anyList()))
                .thenReturn(List.of(dto1, dto2));

        // when
        var result = productService.getProductList(new ProductListServiceRequest(1, 10, "createdAt"));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("상품1");
    }


    @Test
    @DisplayName("성공: 인기 상품 조회")
    void get_best_products_success() {
        // given
        Product product = Product.builder()
                .id(1L)
                .name("인기상품")
                .price(3000L)
                .state(1)
                .orderOptions(List.of())
                .createdAt("2025-04-01 12:00:00")
                .build();

        when(productRepository.findPopularTop5()).thenReturn(List.of(product));

        ProductServiceResponse dto = new ProductServiceResponse(
                1L,
                "인기상품",
                3000L,
                1,
                "2025-04-01 12:00:00",
                List.of() // options
        );

        when(productMapper.productsToProductServiceResponses(List.of(product)))
                .thenReturn(List.of(dto));

        // when
        var result = productService.getBestProducts();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("인기상품");
    }


    @Test
    @DisplayName("성공: 인기 상품 점수 재계산")
    void calculate_best_products_success() {
        // when
        productService.calculateBestProducts();

        // then
        verify(productRepository, times(1)).recalculateBestProducts();
    }

    @Test
    @DisplayName("실패: 인기 상품 조회 - 인기 상품 없음")
    void get_best_products_fail_empty() {
        // given
        when(productRepository.findPopularTop5()).thenReturn(List.of());

        // when
        var result = productService.getBestProducts();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("실패: 인기 상품 점수 재계산 중 예외 발생")
    void calculate_best_products_fail_by_exception() {
        // given
        doThrow(new RuntimeException("DB error")).when(productRepository).recalculateBestProducts();

        // then
        assertThrows(RuntimeException.class, () -> productService.calculateBestProducts());
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
    @DisplayName("성공: 상품은 조회됐지만 OrderItem이 없으면 총합은 0")
    void calculateTotalPrice_when_no_orderItems() {
        // given
        ProductOptionKeyDto item = new ProductOptionKeyDto(1L, 1L, 1L);
        List<ProductOptionKeyDto> request = List.of(item);

        Product product = Product.builder()
                .id(1L)
                .price(1000L)
                .orderItems(List.of())  // orderItems 비어 있음
                .createdAt("2025-04-10T12:00:00")
                .build();

        when(productRepository.findByIds(List.of(1L)))
                .thenReturn(List.of(product));

        // when
        ProductTotalPriceResponse response = productService.calculateTotalPrice(request);

        // then
        assertThat(response.totalPrice()).isEqualTo(0L);
    }

    @Test
    @DisplayName("성공: 조회된 Product가 하나도 없으면 총합은 0")
    void calculateTotalPrice_when_productList_is_empty() {
        // given
        ProductOptionKeyDto item = new ProductOptionKeyDto(99L, 1L, 1L);
        List<ProductOptionKeyDto> request = List.of(item);

        when(productRepository.findByIds(List.of(99L)))
                .thenReturn(List.of());  // 빈 productList

        // when
        ProductTotalPriceResponse response = productService.calculateTotalPrice(request);

        // then
        assertThat(response.totalPrice()).isEqualTo(0L);
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
    @DisplayName("성공: 요청된 item(productId + optionId)만 총합 계산")
    void calculateTotalPrice_with_selected_items_only() {
        // given
        Product product = Product.builder()
                .id(1L)
                .price(1000L)
                .createdAt("2025-04-10T12:00:00")
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .optionId(11L)
                .eachPrice(100L)
                .quantity(1)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        OrderItem item2 = OrderItem.builder()
                .id(2L)
                .userId(1L)
                .productId(1L)
                .optionId(12L)
                .eachPrice(300L)
                .quantity(1)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        product.setOrderItems(List.of(item1, item2));

        List<ProductOptionKeyDto> request = List.of(
                new ProductOptionKeyDto(1L, 11L, 1L),  // matches item1
                new ProductOptionKeyDto(1L, 12L, 2L)   // matches item2
        );

        when(productRepository.findByIds(anyList())).thenReturn(List.of(product));

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(100L + 300L); // 400
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
    @DisplayName("성공: ProductListServiceDto 응답 받기")
    void getProductByIds_success() {
        // given
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 1L, 1L);
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(2L, 2L, 1L);
        List<ProductOptionKeyDto> request = List.of(item1, item2);

        Product product1 = Product.builder().id(1L).name("상품1").price(1000L).state(1).createdAt("2025-04-10T12:00:00").build();
        Product product2 = Product.builder().id(2L).name("상품2").price(2000L).state(1).createdAt("2025-04-10T12:00:00").build();
        List<Product> products = List.of(product1, product2);

        ProductServiceResponse dto1 = new ProductServiceResponse(1L, "상품1", 1000L, 1, "2025-04-10T12:00:00", List.of());
        ProductServiceResponse dto2 = new ProductServiceResponse(2L, "상품2", 2000L, 1, "2025-04-10T12:00:00", List.of());

        List<Long> productIds = List.of(item1.productId(), item2.productId());
        when(productMapper.extractProductIds(request)).thenReturn(productIds);
        when(productRepository.findByIds(productIds)).thenReturn(products);
        when(productMapper.productsToProductServiceResponses(products)).thenReturn(List.of(dto1, dto2));

        // when
        List<ProductServiceResponse> result = productService.getProductByIds(request);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("상품1");
        assertThat(result.get(1).price()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("성공: 총 금액 계산")
    void calculateTotalPrice_success() {
        // given
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 1L, 1L);
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(2L, 2L, 1L);
        List<ProductOptionKeyDto> request = List.of(item1, item2);

        OrderItem orderItem1 = OrderItem.of(1L, 1L, 1L, 1000L, 2);
        OrderItem orderItem2 = OrderItem.of(1L, 2L, 2L, 2000L, 3);
        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

        Product product1 = Product.builder()
                .id(1L)
                .price(1000L)
                .orderItems(List.of(orderItem1))
                .createdAt("2025-04-10T12:00:00")
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .price(2000L)
                .orderItems(List.of(orderItem2))
                .createdAt("2025-04-10T12:00:00")
                .build();

        List<Long> productIds = List.of(item1.productId(), item2.productId());
        when(productMapper.extractProductIds(request)).thenReturn(productIds);

        when(productRepository.findByIds(List.of(1L, 2L))).thenReturn(List.of(product1, product2));

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(2000 + 6000);
    }

}
