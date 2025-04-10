package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.common.exception.ResourceNotFoundException;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.product.dto.*;
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

        when(productRepository.findById(1L)).thenReturn(product);

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
        Product product1 = Product.builder().id(1L).name("상품1").price(1000L).state(1).orderOptions(List.of()).createdAt("2025-04-01 12:00:00").build();
        Product product2 = Product.builder().id(2L).name("상품2").price(2000L).state(1).orderOptions(List.of()).createdAt("2025-04-01 12:00:00").build();

        when(productRepository.findAll(any())).thenReturn(List.of(product1, product2));

        // when
        var result = productService.getProductList(new ProductListServiceRequest(1, 10, "createdAt"));

        // then
        assertThat(result.products()).hasSize(2);
        assertThat(result.products().get(0).name()).isEqualTo("상품1");
    }

    @Test
    @DisplayName("성공: 인기 상품 조회")
    void get_best_products_success() {
        // given
        Product product = Product.builder().id(1L).name("인기상품").price(3000L).state(1).orderOptions(List.of()).createdAt("2025-04-01 12:00:00").build();
        when(productRepository.findPopularTop5()).thenReturn(List.of(product));

        // when
        var result = productService.getBestProducts();

        // then
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().get(0).name()).isEqualTo("인기상품");
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
    @DisplayName("실패: 상품 리스트 조회 - 유효하지 않은 정렬 조건")
    void get_product_list_fail_invalid_sort() {
        // given
        when(productRepository.findAll(any()))
                .thenThrow(new IllegalArgumentException("Invalid sort parameter"));

        // then
        assertThrows(IllegalArgumentException.class, () ->
                productService.getProductList(new ProductListServiceRequest(1, 10, "invalidField")));
    }

    @Test
    @DisplayName("실패: 인기 상품 조회 - 인기 상품 없음")
    void get_best_products_fail_empty() {
        // given
        when(productRepository.findPopularTop5()).thenReturn(List.of());

        // when
        var result = productService.getBestProducts();

        // then
        assertThat(result.products()).isEmpty();
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
    @DisplayName("성공: ProductListSvcByIdsRequest로 ProductListServiceDto 응답 받기 - mock 방식")
    void getProductByIds_success() {
        // given
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 1L);
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(2L, 2L);
        ProductListSvcByIdsRequest request = new ProductListSvcByIdsRequest(List.of(item1, item2));

        Product product1 = Product.builder().id(1L).name("상품1").price(1000L).state(1).createdAt("2025-04-10T12:00:00").build();
        Product product2 = Product.builder().id(2L).name("상품2").price(2000L).state(1).createdAt("2025-04-10T12:00:00").build();

        ProductServiceResponse dto1 = new ProductServiceResponse(1L, "상품1", 1000L, 1, "2025-04-10T12:00:00", List.of());
        ProductServiceResponse dto2 = new ProductServiceResponse(2L, "상품2", 2000L, 1, "2025-04-10T12:00:00", List.of());

        when(productRepository.findByIds(List.of(1L, 2L)))
                .thenReturn(List.of(product1, product2));
        when(productMapper.productsToProductServiceResponses(List.of(product1, product2)))
                .thenReturn(List.of(dto1, dto2));

        // when
        ProductListServiceDto result = productService.getProductByIds(request);

        // then
        assertThat(result.products()).hasSize(2);
        assertThat(result.products().get(0).name()).isEqualTo("상품1");
        assertThat(result.products().get(1).price()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("성공: ProductListSvcByIdsRequest로 총 금액 계산 - mock 방식")
    void calculateTotalPrice_success() {
        // given
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 1L);
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(2L, 2L);
        ProductListSvcByIdsRequest request = new ProductListSvcByIdsRequest(List.of(item1, item2));

        OrderItem orderItem1 = OrderItem.of(1L, null, 1L, 1L, 1000L, 2); // 1000 * 2 = 2000
        OrderItem orderItem2 = OrderItem.of(1L, null, 2L, 2L, 2000L, 3); // 2000 * 3 = 6000

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

        when(productRepository.findByIds(List.of(1L, 2L)))
                .thenReturn(List.of(product1, product2));

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(2000 + 6000);
    }

    @Test
    @DisplayName("실패: ProductListSvcByIdsRequest가 null 리스트를 가지면 예외 발생")
    void getProductByIds_fail_null_list() {
        // expect
        assertThatThrownBy(() -> new ProductListSvcByIdsRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 목록은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: ProductListSvcByIdsRequest가 빈 리스트면 예외 발생")
    void getProductByIds_fail_empty_list() {
        // expect
        assertThatThrownBy(() -> new ProductListSvcByIdsRequest(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 목록은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("성공: 상품은 조회됐지만 OrderItem이 없으면 총합은 0")
    void calculateTotalPrice_when_no_orderItems() {
        // given
        ProductOptionKeyDto item = new ProductOptionKeyDto(1L, 1L);
        ProductListSvcByIdsRequest request = new ProductListSvcByIdsRequest(List.of(item));

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
        ProductOptionKeyDto item = new ProductOptionKeyDto(99L, 1L);
        ProductListSvcByIdsRequest request = new ProductListSvcByIdsRequest(List.of(item));

        when(productRepository.findByIds(List.of(99L)))
                .thenReturn(List.of());  // 빈 productList

        // when
        ProductTotalPriceResponse response = productService.calculateTotalPrice(request);

        // then
        assertThat(response.totalPrice()).isEqualTo(0L);
    }
}
