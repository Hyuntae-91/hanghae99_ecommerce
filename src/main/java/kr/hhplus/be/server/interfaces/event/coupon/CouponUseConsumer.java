package kr.hhplus.be.server.interfaces.event.coupon;

import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.coupon.dto.request.CouponUseRequest;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponUsePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponUseConsumer {

    private final CouponService couponService;

    @KafkaListener(topics = Topics.COUPON_USE_TOPIC, groupId = Groups.COUPON_USE_GROUP)
    public void consume(@Payload CouponUsePayload event) {
        couponService.applyCouponUse(new CouponUseRequest(event.userId(), event.couponIssueId(), 1));
    }
}
