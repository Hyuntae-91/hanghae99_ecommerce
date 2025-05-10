package kr.hhplus.be.server.common.scheduler;

import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BestProductScheduler {

    private final ProductService productService;

    // 6시간마다 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 0,6,12,18 * * *")
    public void calculateBestProductsPeriodically() {
        List<ProductServiceResponse>  result = productService.calculateBestProducts();
    }
}