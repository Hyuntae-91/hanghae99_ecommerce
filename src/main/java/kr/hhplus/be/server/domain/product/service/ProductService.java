package kr.hhplus.be.server.domain.product.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.product.dto.request.BestProductRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.model.ProductRanking;
import kr.hhplus.be.server.domain.product.model.mapper.ProductScoreMapper;
import kr.hhplus.be.server.domain.product.repository.ProductRankingRedisRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.ProductStates;
import kr.hhplus.be.server.domain.product.dto.*;
import kr.hhplus.be.server.domain.product.dto.request.ProductListServiceRequest;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.domain.product.dto.request.ProductServiceRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductRankingRedisRepository productRankingRedisRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderOptionRepository orderOptionRepository;
    private final ProductMapper productMapper;

    private ProductServiceResponse toResponseWithOptions(Product product) {
        List<OrderOption> orderOptions = orderOptionRepository.findByProductId(product.getId());
        List<ProductOptionResponse> optionResponses = productMapper.toProductOptionResponseList(orderOptions);
        return productMapper.productToProductServiceResponse(product).withOptions(optionResponses);
    }

    private void expireCacheKey(String cacheKey) {
        LocalDateTime tomorrowMidnight = LocalDate.now()
                .plusDays(1)
                .atStartOfDay();

        Instant expireAt = tomorrowMidnight.toInstant(ZoneOffset.UTC);

        productRankingRedisRepository.expireAt(cacheKey, expireAt);

        log.info("[ProductService] Set expireAt for cacheKey={} at {}", cacheKey, tomorrowMidnight);
    }

    private List<ProductServiceResponse> getBestProductsByRankingKey(String key, BestProductRequest request) {
        Set<ZSetOperations.TypedTuple<Object>> entries = productRankingRedisRepository.findTopNWithScores(key, 100);

        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        ProductRanking ranking = ProductRanking.fromSnapshots(List.of(entries));
        ProductRanking slicedRanking = ranking.slice(request.page(), request.size());

        List<Long> slicedProductIds = ProductScoreMapper.toProductIdList(slicedRanking.scores());

        if (slicedProductIds.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findByIds(slicedProductIds);

        return productMapper.toSortedProductServiceResponses(products, slicedProductIds);
    }

    public ProductServiceResponse getProductById(ProductServiceRequest requestDto) {
        Product product = productRepository.findById(requestDto.productId());

        List<OrderOption> orderOptions = orderOptionRepository.findByProductId(product.getId());
        List<ProductOptionResponse> optionResponses = productMapper.toProductOptionResponseList(orderOptions);

        ProductServiceResponse baseResponse = productMapper.productToProductServiceResponse(product);
        return baseResponse.withOptions(optionResponses);
    }

    public List<ProductServiceResponse> getProductByIds(List<ProductOptionKeyDto> requestDto) {
        List<Product> productList = productRepository.findByIds(productMapper.extractProductIds(requestDto));
        return productMapper.productsToProductServiceResponses(productList);
    }

    @Cacheable(
            value = "productList",
            key = "'productList::page=' + #root.args[0].page() + ':size=' + #root.args[0].size() + ':sort=' + #root.args[0].sort()"
    )
    public List<ProductServiceResponse> getProductList(ProductListServiceRequest requestDto) {
        List<Integer> excludedStates = List.of(
                ProductStates.DELETED.getCode(),
                ProductStates.SOLD_OUT.getCode()
        );

        List<Product> productList = productRepository.findByStateNotIn(
                requestDto.page(),
                requestDto.size(),
                requestDto.sort(),
                excludedStates
        );

        return productList.stream()
                .map(this::toResponseWithOptions)
                .toList();
    }

    @CircuitBreaker(name = "dailyBestProducts", fallbackMethod = "fallbackDaily")
    @Cacheable(
            value = "productDailyBest",
            key = "'dailyBest::page=' + #root.args[0].page() + ':size=' + #root.args[0].size()"
    )
    public List<ProductServiceResponse> getDailyBestProducts(BestProductRequest request) {
        List<ProductServiceResponse> response = getBestProductsByRankingKey(
                "product:score:" + LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                request
        );
        expireCacheKey("productDailyBest::dailyBest::page=" + request.page() + ":size=" + request.size());
        return response;
    }

    @CircuitBreaker(name = "weeklyBestProducts", fallbackMethod = "fallbackWeekly")
    @Cacheable(
            value = "productWeeklyBest",
            key = "'weeklyBest::page=' + #root.args[0].page() + ':size=' + #root.args[0].size()"
    )
    public List<ProductServiceResponse> getWeeklyBestProducts(BestProductRequest request) {
        List<ProductServiceResponse> response = getBestProductsByRankingKey(
                "product:score:week",
                request
        );
        expireCacheKey("productWeeklyBest::weeklyBest::page=" +request.page() + ":size=" + request.size());
        return response;
    }

    public ProductTotalPriceResponse calculateTotalPrice(List<ProductOptionKeyDto> requestDto) {
        long total = 0;
        for (ProductOptionKeyDto dto : requestDto) {
            OrderItem item = orderItemRepository.findById(dto.itemId());
            total += item.calculateTotalPrice();
        }

        return new ProductTotalPriceResponse(total);
    }

    public List<Product> fallbackDaily(BestProductRequest request, Throwable t) {
        log.error("fallbackDaily triggered: {}", t.getMessage(), t);
        return List.of(); // 빈 리스트 반환
    }

    public List<Product> fallbackWeekly(BestProductRequest request, Throwable t) {
        log.error("fallbackWeekly triggered: {}", t.getMessage(), t);
        return List.of(); // 빈 리스트 반환
    }
}