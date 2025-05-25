package kr.hhplus.be.server.domain.point.event;

import kr.hhplus.be.server.domain.coupon.dto.event.ApplyCouponDiscountCompletedEvent;
import kr.hhplus.be.server.domain.point.dto.response.PointUseServiceResponse;
import kr.hhplus.be.server.domain.point.mapper.UserPointMapper;
import kr.hhplus.be.server.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PointEventHandler {

    private final PointService pointService;
    private final UserPointMapper userPointMapper;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUsePointRequested(ApplyCouponDiscountCompletedEvent event) {
        PointUseServiceResponse response = pointService.use(userPointMapper.toPointUseServiceRequest(event));

        eventPublisher.publishEvent(
            userPointMapper.toPointUsedCompletedEvent(event, response)
        );
    }
}
