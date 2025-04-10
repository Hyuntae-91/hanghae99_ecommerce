package kr.hhplus.be.server.infrastructure.mock;

import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockDataPlatformListener {

    @EventListener
    public void handle(PaymentCompletedEvent event) {
        // 실제로는 외부 API 전송, 여기선 로그로 Mock 처리
        log.info("[MOCK] 전송됨 → 외부 데이터 플랫폼에 주문 정보: paymentId={}, orderId={}, amount={}",
                event.payment().paymentId(),
                event.payment().orderId(),
                event.payment().totalPrice()
        );
    }
}