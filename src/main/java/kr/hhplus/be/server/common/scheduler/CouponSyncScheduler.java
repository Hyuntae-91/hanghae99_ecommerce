package kr.hhplus.be.server.common.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponSyncScheduler {

    private final CouponService couponService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 00시 5분
    public void syncCouponsToRedis() throws JsonProcessingException {
        log.info("[CouponSyncScheduler] 유효 쿠폰 Redis 저장 시작");

        couponService.syncAllActiveCouponsToRedis();

        log.info("[CouponSyncScheduler] 유효 쿠폰 Redis 저장 완료");
    }
}