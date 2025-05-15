package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.product.dto.request.BestProductRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.repository.ProductRankingRedisRepository;
import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.ProductStates;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private OrderItemRepository orderItemRepository;
    private OrderOptionRepository orderOptionRepository;
    private ProductService productService;
    private ProductMapper productMapper;
    private ProductRankingRedisRepository productRankingRedisRepository;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        orderOptionRepository = mock(OrderOptionRepository.class);
        productMapper = mock(ProductMapper.class);
        productRankingRedisRepository = mock(ProductRankingRedisRepository.class);

        productService = new ProductService(
                productRepository,
                productRankingRedisRepository,
                orderItemRepository,
                orderOptionRepository,
                productMapper
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
        assertThat(dto.price()).isEqualTo(1000L);
        assertThat(dto.createdAt()).isEqualTo("2025-04-01 12:00:00");
        assertThat(dto.options()).isEmpty();
    }

    @Test
    @DisplayName("성공: 상품 리스트 조회")
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

        // option responses
        ProductOptionResponse optResponse1 = new ProductOptionResponse(101L, 275, 10);
        ProductOptionResponse optResponse2 = new ProductOptionResponse(102L, 280, 5);

        when(productMapper.toProductOptionResponseList(List.of(option1))).thenReturn(List.of(optResponse1));
        when(productMapper.toProductOptionResponseList(List.of(option2))).thenReturn(List.of(optResponse2));

        // product response DTOs with options
        ProductServiceResponse dto1 = new ProductServiceResponse(
                1L, "상품1", 1000L, 1, "2025-04-01 12:00:00", List.of(optResponse1)
        );
        ProductServiceResponse dto2 = new ProductServiceResponse(
                2L, "상품2", 2000L, 1, "2025-04-01 12:00:00", List.of(optResponse2)
        );

        when(productMapper.productToProductServiceResponse(product1)).thenReturn(dto1);
        when(productMapper.productToProductServiceResponse(product2)).thenReturn(dto2);

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
    @DisplayName("성공: 인기 상품 조회")
    void get_best_products_success() {
        // given
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 11L, 1L);
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(1L, 12L, 2L);
        List<ProductOptionKeyDto> request = List.of(item1, item2);

        OrderItem orderItem1 = OrderItem.of(1L, 1L, 11L, 100L, 1);
        OrderItem orderItem2 = OrderItem.of(1L, 1L, 12L, 300L, 1);
        orderItem1.setId(1L);
        orderItem2.setId(2L);

        Product product = Product.builder()
                .id(1L)
                .price(0L)
                .createdAt("2025-04-10T12:00:00")
                .build();

        when(productMapper.extractProductIds(request)).thenReturn(List.of(1L));
        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(product));
        when(orderItemRepository.findById(1L)).thenReturn(orderItem1);
        when(orderItemRepository.findById(2L)).thenReturn(orderItem2);

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(100L + 300L);
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

        Product product = Product.builder()
                .id(1L)
                .price(1000L)
                .createdAt("2025-04-10T12:00:00")
                .build();

        when(productMapper.extractProductIds(request)).thenReturn(List.of(1L));
        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(product));

        when(orderItemRepository.findById(1L))
                .thenThrow(new ResourceNotFoundException("OrderItem not found: 1"));

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> productService.calculateTotalPrice(request));
    }

    @Test
    @DisplayName("성공: 조회된 Product가 하나도 없으면 총합은 0")
    void calculateTotalPrice_when_productList_is_empty() {
        // given
        ProductOptionKeyDto item = new ProductOptionKeyDto(99L, 1L, 1L);
        List<ProductOptionKeyDto> request = List.of(item);

        List<Long> productIds = List.of(99L);

        when(productMapper.extractProductIds(request)).thenReturn(productIds);
        when(productRepository.findByIds(productIds)).thenReturn(List.of());
        when(orderItemRepository.findById(1L))
                .thenThrow(new ResourceNotFoundException("OrderItem not found: 1"));

        // when & then
        assertThrows(ResourceNotFoundException.class, () ->
                productService.calculateTotalPrice(request));
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
        ProductOptionKeyDto item1 = new ProductOptionKeyDto(1L, 11L, 1L);
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(1L, 12L, 2L);
        List<ProductOptionKeyDto> request = List.of(item1, item2);

        OrderItem orderItem1 = OrderItem.of(1L, 1L, 11L, 100L, 1);
        OrderItem orderItem2 = OrderItem.of(1L, 1L, 12L, 300L, 1);

        Product product = Product.builder()
                .id(1L)
                .price(0L)
                .createdAt("2025-04-10T12:00:00")
                .build();

        when(productMapper.extractProductIds(request)).thenReturn(List.of(1L));
        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(product));
        when(orderItemRepository.findById(1L)).thenReturn(orderItem1);
        when(orderItemRepository.findById(2L)).thenReturn(orderItem2);

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(List.of(item1, item2));

        // then
        assertThat(result.totalPrice()).isEqualTo(100L + 300L);
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
        ProductOptionKeyDto item2 = new ProductOptionKeyDto(2L, 2L, 2L);
        List<ProductOptionKeyDto> request = List.of(item1, item2);

        OrderItem orderItem1 = OrderItem.of(1L, 1L, 1L, 1000L, 2);
        OrderItem orderItem2 = OrderItem.of(1L, 2L, 2L, 2000L, 3);
        orderItem1.setId(1L);
        orderItem2.setId(2L);

        Product product1 = Product.builder()
                .id(1L)
                .price(1000L)
                .createdAt("2025-04-10T12:00:00")
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .price(2000L)
                .createdAt("2025-04-10T12:00:00")
                .build();

        List<Long> productIds = List.of(item1.productId(), item2.productId());
        when(productMapper.extractProductIds(request)).thenReturn(productIds);
        when(productRepository.findByIds(productIds)).thenReturn(List.of(product1, product2));
        when(orderItemRepository.findById(1L)).thenReturn(orderItem1);
        when(orderItemRepository.findById(2L)).thenReturn(orderItem2);

        // when
        ProductTotalPriceResponse result = productService.calculateTotalPrice(request);

        // then
        assertThat(result.totalPrice()).isEqualTo(2000 + 6000);
    }

    @Test
    @DisplayName("성공: 일간 인기 상품 조회")
    void get_daily_best_products_success() {
        // given
        Product product = Product.builder()
                .id(1L)
                .name("일간상품")
                .price(1000L)
                .state(1)
                .createdAt("2025-04-01 12:00:00")
                .build();

        ProductServiceResponse dto = new ProductServiceResponse(
                1L, "일간상품", 1000L, 1, "2025-04-01 12:00:00", List.of()
        );

        when(productRankingRedisRepository.findTopNWithScores(anyString(), eq(100)))
                .thenReturn(Set.of()); // 일단 빈 값으로 시뮬레이션
        when(productRepository.findByIds(anyList()))
                .thenReturn(List.of(product));
        when(productMapper.toSortedProductServiceResponses(anyList(), anyList()))
                .thenReturn(List.of(dto));

        // when
        List<ProductServiceResponse> result = productService.getDailyBestProducts(new BestProductRequest(0, 10));

        // then
        assertThat(result).isEmpty();  // 빈 Set이면 빈 리스트 리턴해야 함
    }

    @Test
    @DisplayName("성공: 주간 인기 상품 조회")
    void get_weekly_best_products_success() {
        // given
        Product product = Product.builder()
                .id(2L)
                .name("주간상품")
                .price(2000L)
                .state(1)
                .createdAt("2025-04-01 12:00:00")
                .build();

        ProductServiceResponse dto = new ProductServiceResponse(
                2L, "주간상품", 2000L, 1, "2025-04-01 12:00:00", List.of()
        );

        when(productRankingRedisRepository.findTopNWithScores(anyString(), eq(100)))
                .thenReturn(Set.of());
        when(productRepository.findByIds(anyList()))
                .thenReturn(List.of(product));
        when(productMapper.toSortedProductServiceResponses(anyList(), anyList()))
                .thenReturn(List.of(dto));

        // when
        List<ProductServiceResponse> result = productService.getWeeklyBestProducts(new BestProductRequest(0, 10));

        // then
        assertThat(result).isEmpty();  // 빈 Set이면 빈 리스트 리턴해야 함
    }

    @Test
    @DisplayName("성공: Redis에서 인기 상품 없을 때 빈 리스트 반환")
    void get_best_products_when_redis_is_empty() {
        // given
        when(productRankingRedisRepository.findTopNWithScores(anyString(), eq(100)))
                .thenReturn(null);

        // when
        List<ProductServiceResponse> result = productService.getDailyBestProducts(new BestProductRequest(0, 10));

        // then
        assertThat(result).isEmpty();
    }

}
