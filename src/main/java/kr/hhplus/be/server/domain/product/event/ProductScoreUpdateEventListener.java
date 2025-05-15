package kr.hhplus.be.server.domain.product.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductScoreUpdateEventListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PRODUCT_CURRENT_KEY = "product:score:current";
    private static final double SALE_SCORE = 5.0;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductSoldEvent(ProductSoldEvent event) {
        for (Long productId : event.productIds()) {
            redisTemplate.opsForZSet().incrementScore(PRODUCT_CURRENT_KEY, productId.toString(), SALE_SCORE);
            log.info("[ProductScoreUpdateEventListener] Increased score for productId={} by {}", productId, SALE_SCORE);
        }
    }
}
