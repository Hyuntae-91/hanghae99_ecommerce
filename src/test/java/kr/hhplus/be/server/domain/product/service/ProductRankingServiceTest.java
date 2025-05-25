package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.assembler.ProductAssembler;
import kr.hhplus.be.server.domain.product.dto.request.BestProductRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.model.ProductScore;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductRankingServiceTest {

    private ProductRepository productRepository;
    private ProductAssembler productAssembler;
    private ProductRankingService productRankingService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productAssembler = mock(ProductAssembler.class);

        productRankingService = new ProductRankingService(
                productRepository,
                productAssembler
        );
    }

    @Test
    @DisplayName("성공: 일간 인기 상품 조회")
    void getDailyBestProducts_success() {
        // given
        BestProductRequest request = new BestProductRequest(1, 10);
        List<Product> products = List.of(mock(Product.class));
        List<ProductServiceResponse> responses = List.of(mock(ProductServiceResponse.class));

        when(productRepository.getDailyPageProducts(1, 10)).thenReturn(products);
        when(productAssembler.toResponses(products)).thenReturn(responses);

        // when
        List<ProductServiceResponse> result = productRankingService.getDailyBestProducts(request);

        // then
        assertThat(result).isEqualTo(responses);
        verify(productRepository).expireAt(contains("dailyBest"), any());
    }

    @Test
    @DisplayName("성공: 주간 인기 상품 조회")
    void getWeeklyBestProducts_success() {
        BestProductRequest request = new BestProductRequest(1, 10);
        List<Product> products = List.of(mock(Product.class));
        List<ProductServiceResponse> responses = List.of(mock(ProductServiceResponse.class));

        when(productRepository.getWeeklyPageProducts(1, 10)).thenReturn(products);
        when(productAssembler.toResponses(products)).thenReturn(responses);

        List<ProductServiceResponse> result = productRankingService.getWeeklyBestProducts(request);

        assertThat(result).isEqualTo(responses);
        verify(productRepository).expireAt(contains("weeklyBest"), any());
    }

    @Test
    @DisplayName("성공: 일일 랭킹 업데이트")
    void updateDailyProductRanking_success() {
        when(productRepository.copyKey(any(), any())).thenReturn(true);
        when(productRepository.getTopN(any(), anyInt())).thenReturn(List.of(
                new ProductScore(1L, 50.0)
        ));

        productRankingService.updateDailyProductRanking();

        verify(productRepository).replaceNewRanking(any(), any());
    }

    @Test
    @DisplayName("성공: 주간 랭킹 생성")
    void generateWeeklyRanking_success() {
        List<ProductScore> weeklyScores = List.of(new ProductScore(1L, 100.0));
        when(productRepository.getTopNProductsFromLast7Days(anyInt()))
                .thenReturn(weeklyScores);

        productRankingService.generateWeeklyRanking();

        verify(productRepository).replaceNewRanking(any(), eq(weeklyScores));
        verify(productRepository).expireAt(contains("product:score:week"), any());
    }

    @Test
    @DisplayName("성공: fallbackDaily 호출 시 빈 리스트 반환")
    void fallbackDaily_shouldReturnEmptyList() {
        List<Product> result = productRankingService.fallbackDaily(
                new BestProductRequest(1, 10),
                new RuntimeException("test fallback")
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공: fallbackWeekly 호출 시 빈 리스트 반환")
    void fallbackWeekly_shouldReturnEmptyList() {
        List<Product> result = productRankingService.fallbackWeekly(
                new BestProductRequest(1, 10),
                new RuntimeException("test fallback")
        );

        assertThat(result).isEmpty();
    }
}