package kr.hhplus.be.server.common.scheduler;

import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpireScheduler {

    private final CouponIssueJpaRepository couponIssueJpaRepository;

    @Scheduled(cron = "0 */5 * * * *")
    public void expireCouponScheduler() {
        runScheduler();
    }

    public void runScheduler() {
        String now = LocalDateTime.now().toString();

        // 모든 사용 가능 상태 쿠폰 가져오기
        List<CouponIssue> issuedCoupons = couponIssueJpaRepository.findAll();
        long expiredCount = 0;

        for (CouponIssue issue : issuedCoupons) {
            if (issue.getState() == 0 && issue.getEndAt().compareTo(now) < 0) {
                issue.updateState(2);  // 2 = 만료 상태로 간주
                expiredCount++;
            }
        }

        log.info("[Scheduler] 만료 처리된 쿠폰 수: {}", expiredCount);
    }
}
