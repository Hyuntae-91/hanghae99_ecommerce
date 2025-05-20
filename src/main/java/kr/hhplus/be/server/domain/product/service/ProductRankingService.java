package kr.hhplus.be.server.domain.product.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import kr.hhplus.be.server.domain.product.assembler.ProductAssembler;
import kr.hhplus.be.server.domain.product.dto.request.BestProductRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.model.ProductRanking;
import kr.hhplus.be.server.domain.product.model.ProductScore;
import kr.hhplus.be.server.domain.product.model.vo.ProductCacheKey;
import kr.hhplus.be.server.domain.product.model.vo.ProductRankingKey;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRankingService {

    private final ProductRepository productRepository;
    private final ProductAssembler productAssembler;

    private static final LocalDate today = LocalDate.now();

    @CircuitBreaker(name = "dailyBestProducts", fallbackMethod = "fallbackDaily")
    @Cacheable(
            value = "productDailyBest",
            key = "'dailyBest::page=' + #root.args[0].page() + ':size=' + #root.args[0].size()"
    )
    public List<ProductServiceResponse> getDailyBestProducts(BestProductRequest request) {
        List<Product> products = productRepository.getDailyPageProducts(request.page(), request.size());

        ProductCacheKey cacheKey = ProductCacheKey.dailyBest(request.page(), request.size());
        LocalDateTime tomorrowMidnight = LocalDate.now().plusDays(1).atStartOfDay();
        Instant expireAt = tomorrowMidnight.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        productRepository.expireAt(cacheKey.value(), expireAt);
        log.info("[Daily Best Products] Set TTL for {} to {}", cacheKey, tomorrowMidnight);

        return productAssembler.toResponses(products);
    }

    @CircuitBreaker(name = "weeklyBestProducts", fallbackMethod = "fallbackWeekly")
    @Cacheable(
            value = "productWeeklyBest",
            key = "'weeklyBest::page=' + #root.args[0].page() + ':size=' + #root.args[0].size()"
    )
    public List<ProductServiceResponse> getWeeklyBestProducts(BestProductRequest request) {
        List<Product> products = productRepository.getWeeklyPageProducts(request.page(), request.size());

        ProductCacheKey cacheKey = ProductCacheKey.weeklyBest(request.page(), request.size());
        LocalDateTime tomorrowMidnight = LocalDate.now().plusDays(1).atStartOfDay();
        Instant expireAt = tomorrowMidnight.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        productRepository.expireAt(cacheKey.value(), expireAt);
        log.info("[Weekly Best Products] Set TTL for {} to {}", cacheKey, tomorrowMidnight);

        return productAssembler.toResponses(products);
    }

    public void updateDailyProductRanking() {
        log.info("[ProductRankingService] Start ranking update at 23:50");

        String todaySnapshotKey = ProductRankingKey.ofDaily(LocalDate.now()).value();
        String todayTempKey = ProductRankingKey.tempKey(LocalDate.now());
        String currentKey = ProductRankingKey.currentKey();

        // Step 1: product:score:current -> 오늘 날짜로 복제
        if (!productRepository.copyKey(currentKey, todaySnapshotKey)) {
            log.info("No current ranking key exists. Skipping ranking update.");
            return;
        }
        log.info("Copied product:current to snapshot key: {}", todaySnapshotKey);

        LocalDateTime tomorrowMidnight = LocalDate.now().plusDays(8).atStartOfDay();
        Instant expireAt = tomorrowMidnight.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        productRepository.expireAt(todaySnapshotKey, expireAt);
        log.info("Set TTL 8 days for snapshot key: {}", todaySnapshotKey);

        // Step 2: snapshot 복제 → temp 키
        productRepository.copyKey(todaySnapshotKey, todayTempKey);
        log.info("Copied snapshot to temp key: {}", todayTempKey);

        // Step 3: top 100개 정보 가져온다
        List<ProductScore> topN = productRepository.getTopN(todayTempKey, ProductRanking.MAX_RANK_SIZE);

        List<ProductScore> filteredAndDecayed = new ArrayList<>();
        for (ProductScore score : topN) {
            if (score.isBelowThreshold()) {
                log.info("Removed productId={} due to low score={}", score.productId(), score.score());
                continue;
            }

            ProductScore decayed = score.decay();
            log.info("Decayed productId={} to new score={}", decayed.productId(), decayed.score());
            filteredAndDecayed.add(decayed);
        }

        productRepository.replaceNewRanking(todayTempKey, filteredAndDecayed);
        log.info("Replaced Redis ZSet with decayed top scores for key={}", todayTempKey);
    }

    public void generateWeeklyRanking() {
        log.info("[ProductRankingService] Start generating weekly ranking");

        List<ProductScore> productRanking = productRepository.getTopNProductsFromLast7Days(ProductRanking.MAX_RANK_SIZE);
        productRepository.replaceNewRanking(ProductRankingKey.weekKey(), productRanking);

        LocalDateTime tomorrowMidnight = LocalDate.now().plusDays(1).atStartOfDay();
        Instant expireAt = tomorrowMidnight.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        productRepository.expireAt(ProductRankingKey.weekKey(), expireAt);

        log.info("Weekly ranking generated and stored to Redis with key: {}", ProductRankingKey.weekKey());
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
