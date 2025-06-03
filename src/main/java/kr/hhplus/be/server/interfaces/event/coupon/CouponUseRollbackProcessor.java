package kr.hhplus.be.server.interfaces.event.coupon;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.coupon.mapper.CouponMapper;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponUseRollbackPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUseRollbackPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponUseRollbackProcessor {

    private final CouponService couponService;
    private final CouponMapper couponMapper;
    private final MessagePublisher<CouponUseRollbackPayload> couponUseRollbackPayloadMessagePublisher;

    @KafkaListener(topics = Topics.USE_USER_POINT_FAILED_TOPIC, groupId = Groups.USE_USER_POINT_FAILED_GROUP)
    public void consume(@Payload PointUseRollbackPayload event) {
        if (event.couponId() != null) {
            couponService.applyCouponRollback(couponMapper.toCouponUseRequest(event, 0));
        }
        couponUseRollbackPayloadMessagePublisher.publish(
                Topics.PRODUCT_TOTAL_PRICE_FAIL_ROLLBACK,
                null,
                couponMapper.toProductTotalPriceFailRollbackPayload(event)
        );
    }

}
