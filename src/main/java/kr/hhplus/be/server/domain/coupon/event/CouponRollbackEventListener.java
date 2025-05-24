package kr.hhplus.be.server.domain.coupon.event;

import kr.hhplus.be.server.domain.coupon.dto.event.CouponRollbackEvent;
import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
@Slf4j
@Async
public class CouponRollbackEventListener {

    private final CouponRedisRepository couponRedisRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleCouponRollback(CouponRollbackEvent event) {
        Long userId = event.userId();
        Long couponId = event.couponId();
        if (couponId == null) {
            log.warn("CouponRollbackEvent received with null couponIssueId");
            return;
        }

        try {
            // 롤백 로직: Redis에 저장된 쿠폰 사용 기록을 초기화
            couponRedisRepository.rollbackCoupon(userId, couponId);

            log.info("쿠폰 롤백 완료 - couponIssueId={}", couponId);
        } catch (Exception e) {
            log.error("쿠폰 롤백 실패 - couponIssueId={}", couponId, e);
            throw e;
        }
    }
}