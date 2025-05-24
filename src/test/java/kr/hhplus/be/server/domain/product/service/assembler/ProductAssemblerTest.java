package kr.hhplus.be.server.domain.product.service.assembler;

import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.product.assembler.ProductAssembler;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.mapper.ProductMapper;
import kr.hhplus.be.server.domain.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductAssemblerTest {

    private ProductMapper productMapper;
    private ProductAssembler productAssembler;

    @BeforeEach
    void setUp() {
        productMapper = mock(ProductMapper.class);
        productAssembler = new ProductAssembler(productMapper);
    }

    @Test
    @DisplayName("성공: Product 단건 -> ProductServiceResponse 변환")
    void toResponse_success() {
        Product product = Product.builder().id(1L).name("상품A").price(1000L).state(1).build();
        ProductServiceResponse expected = new ProductServiceResponse(1L, "상품A", 1000L, 1, "2025-05-23T00:00:00", List.of());

        when(productMapper.productToProductServiceResponse(product)).thenReturn(expected);

        ProductServiceResponse result = productAssembler.toResponse(product);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("성공: Product + OrderOptions -> ProductServiceResponse with options 변환")
    void toResponseWithOptions_success() {
        Product product = Product.builder().id(2L).name("상품B").price(2000L).state(1).build();
        OrderOption option = OrderOption.builder().id(10L).productId(2L).size(275).stockQuantity(10).build();

        ProductServiceResponse base = new ProductServiceResponse(2L, "상품B", 2000L, 1, "2025-05-23T00:00:00", List.of());
        List<ProductOptionResponse> optionResponses = List.of(new ProductOptionResponse(10L, 275, 10));

        when(productMapper.productToProductServiceResponse(product)).thenReturn(base);
        when(productMapper.toProductOptionResponseList(List.of(option))).thenReturn(optionResponses);

        ProductServiceResponse result = productAssembler.toResponseWithOptions(product, List.of(option));

        assertThat(result.options()).hasSize(1);
        assertThat(result.options().get(0).size()).isEqualTo(275);
    }

    @Test
    @DisplayName("성공: Product 리스트 -> ProductServiceResponse 리스트 변환")
    void toResponses_success() {
        Product product1 = Product.builder().id(1L).name("A").price(1000L).state(1).build();
        Product product2 = Product.builder().id(2L).name("B").price(2000L).state(1).build();

        when(productMapper.productToProductServiceResponse(product1)).thenReturn(new ProductServiceResponse(1L, "A", 1000L, 1, "2025-05-23T00:00:00", List.of()));
        when(productMapper.productToProductServiceResponse(product2)).thenReturn(new ProductServiceResponse(2L, "B", 2000L, 1, "2025-05-23T00:00:00", List.of()));

        List<ProductServiceResponse> result = productAssembler.toResponses(List.of(product1, product2));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("성공: ProductOptionKeyDto로부터 productId 리스트 추출")
    void extractProductIds_success() {
        List<ProductOptionKeyDto> input = List.of(
                new ProductOptionKeyDto(1L, 10L, 100L),
                new ProductOptionKeyDto(2L, 20L, 200L),
                new ProductOptionKeyDto(1L, 30L, 300L)  // 중복 productId
        );

        List<Long> result = productAssembler.extractProductIds(input);

        assertThat(result).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("성공: 정렬된 ID 기준 Product 정렬")
    void toSortedResponses_success() {
        Product p1 = Product.builder().id(1L).name("P1").price(1000L).state(1).build();
        Product p2 = Product.builder().id(2L).name("P2").price(2000L).state(1).build();

        when(productMapper.productToProductServiceResponse(p1)).thenReturn(new ProductServiceResponse(1L, "A", 1000L, 1, "2025-05-23T00:00:00", List.of()));
        when(productMapper.productToProductServiceResponse(p2)).thenReturn(new ProductServiceResponse(2L, "B", 2000L, 1, "2025-05-23T00:00:00", List.of()));

        List<ProductServiceResponse> result = productAssembler.toSortedResponses(List.of(p1, p2), List.of(2L, 1L));

        assertThat(result.get(0).id()).isEqualTo(2L);
        assertThat(result.get(1).id()).isEqualTo(1L);
    }
}