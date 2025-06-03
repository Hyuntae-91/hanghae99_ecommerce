package kr.hhplus.be.server.interfaces.event.point;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.point.dto.response.PointUseServiceResponse;
import kr.hhplus.be.server.domain.point.mapper.UserPointMapper;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponApplyCompletedPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUseRollbackPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUsedCompletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponApplyCompletedConsumer {

    private final PointService pointService;
    private final UserPointMapper userPointMapper;
    private final MessagePublisher<PointUsedCompletedPayload> pointUsedCompletedPayloadPublisher;
    private final MessagePublisher<PointUseRollbackPayload> pointUseRollbackPayloadPublisher;

    @KafkaListener(topics = Topics.COUPON_APPLY_COMPLETE_TOPIC, groupId = Groups.COUPON_APPLY_COMPLETE_GROUP)
    public void consume(@Payload CouponApplyCompletedPayload event) {
        try {
            PointUseServiceResponse response = pointService.use(userPointMapper.toPointUseServiceRequest(event));
            pointUsedCompletedPayloadPublisher.publish(
                    Topics.USE_USER_POINT_COMPLETE_TOPIC,
                    null,
                    userPointMapper.toPointUsedCompletedPayload(event, response)
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            pointUseRollbackPayloadPublisher.publish(
                    Topics.USE_USER_POINT_FAILED_TOPIC,
                    null,
                    userPointMapper.toPointUseRollbackPayload(event)
            );
        }
    }

}
