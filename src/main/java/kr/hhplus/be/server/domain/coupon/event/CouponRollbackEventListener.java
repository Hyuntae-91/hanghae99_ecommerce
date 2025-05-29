package kr.hhplus.be.server.domain.coupon.event;

import kr.hhplus.be.server.domain.coupon.dto.event.CouponRollbackEvent;
import kr.hhplus.be.server.domain.coupon.dto.request.CouponUseRequest;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
@Slf4j
public class CouponRollbackEventListener {

    private final CouponService couponService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleCouponRollback(CouponRollbackEvent event) {
        try {
            // 롤백 로직: 쿠폰 사용 기록을 초기화
            couponService.applyCouponUse(new CouponUseRequest(event.userId(), event.couponIssueId(), 0));
            log.info("쿠폰 롤백 완료 - couponIssueId={}", event.couponIssueId());
        } catch (Exception e) {
            log.error("쿠폰 롤백 실패 - couponIssueId={}", event.couponIssueId(), e);
            throw e;
        }
    }
}