package kr.hhplus.be.server.common.scheduler;

import kr.hhplus.be.server.domain.product.service.ProductRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BestProductScheduler {

    private final ProductRankingService productRankingService;

    @Scheduled(cron = "0 50 23 * * *")
    public void calculateProductRanking() {
        productRankingService.updateDailyProductRanking();
    }
}