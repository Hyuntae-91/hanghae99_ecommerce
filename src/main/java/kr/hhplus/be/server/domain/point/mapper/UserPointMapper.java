package kr.hhplus.be.server.domain.point.mapper;

import kr.hhplus.be.server.domain.point.dto.request.PointUseRollbackRequest;
import kr.hhplus.be.server.domain.point.dto.request.PointUseServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.*;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponApplyCompletedPayload;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentRollbackPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUseRollbackPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUsedCompletedPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserPointMapper {

    UserPointMapper INSTANCE = Mappers.getMapper(UserPointMapper.class);

    UserPointServiceResponse toUserPointResponse(UserPoint userPoint);

    PointChargeServiceResponse toUserPointChargeResponse(UserPoint userPoint);

    PointUseServiceResponse toUserPointUseResponse(UserPoint userPoint);

    PointHistoryServiceResponse toUserPointHistoryResponse(PointHistory pointHistory);

    List<PointHistoryServiceResponse> toHistoryListResponse(List<PointHistory> pointHistories);

    PointUseRollbackPayload toPointUseRollbackPayload(PaymentRollbackPayload event);
    PointUseRollbackPayload toPointUseRollbackPayload(CouponApplyCompletedPayload event);

    @Mapping(source = "usedPoint", target = "point")
    PointUseRollbackRequest toPointUseRollbackRequest(PaymentRollbackPayload event);

    default PointUseServiceRequest toPointUseServiceRequest(CouponApplyCompletedPayload event) {
        return new PointUseServiceRequest(event.userId(), event.finalPrice());
    }

    default PointUsedCompletedPayload toPointUsedCompletedPayload(
            CouponApplyCompletedPayload event,
            PointUseServiceResponse response
    ) {
        return new PointUsedCompletedPayload(
                event.orderId(),
                event.userId(),
                response.point(),
                event.couponId(),
                event.couponIssueId(),
                event.items()
        );
    }
}
