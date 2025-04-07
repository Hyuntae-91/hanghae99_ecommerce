package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
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

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        var dto = productService.getProductById(1L);

        // then
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("테스트상품");
    }

    @Test
    @DisplayName("성공: 상품 리스트 조회")
    void get_product_list_success() {
        // given
        Product product1 = Product.builder().id(1L).name("상품1").price(1000L).state(1).createdAt("2025-04-01 12:00:00").build();
        Product product2 = Product.builder().id(2L).name("상품2").price(2000L).state(1).createdAt("2025-04-01 12:00:00").build();

        when(productRepository.findAll(any())).thenReturn(List.of(product1, product2));

        // when
        var result = productService.getProductList(1, 10, "createdAt");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("상품1");
    }

    @Test
    @DisplayName("성공: 인기 상품 조회")
    void get_best_products_success() {
        // given
        Product product = Product.builder().id(1L).name("인기상품").price(3000L).state(1).createdAt("2025-04-01 12:00:00").build();
        when(productRepository.findPopularTop5()).thenReturn(List.of(product));

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
    @DisplayName("실패: 상품 리스트 조회 - 유효하지 않은 정렬 조건")
    void get_product_list_fail_invalid_sort() {
        // given
        when(productRepository.findAll(any()))
                .thenThrow(new IllegalArgumentException("Invalid sort parameter"));

        // then
        assertThrows(IllegalArgumentException.class, () -> productService.getProductList(1, 10, "invalidField"));
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
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // then
        assertThrows(IllegalArgumentException.class, () -> productService.getProductById(99L));
    }

}
