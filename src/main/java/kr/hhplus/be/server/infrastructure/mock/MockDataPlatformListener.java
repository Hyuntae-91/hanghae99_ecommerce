package kr.hhplus.be.server.infrastructure.mock;

import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class MockDataPlatformListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        try {
            // 외부 API 전송 지연을 시뮬레이션
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("MOCK 외부 전송 중 인터럽트 발생", e);
        }

        // 실제로는 외부 API 전송, 여기선 로그로 Mock 처리
        log.info("[MOCK] 전송됨 → 외부 데이터 플랫폼에 주문 정보: paymentId={}, orderId={}, amount={}",
                event.paymentId(),
                event.orderId(),
                event.totalPrice()
        );
    }
}