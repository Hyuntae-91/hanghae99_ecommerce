package kr.hhplus.be.server.domain.point.dto;

import kr.hhplus.be.server.domain.point.dto.response.PointChargeServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.PointHistoryServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.PointUseServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import org.mapstruct.Mapper;
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

}
