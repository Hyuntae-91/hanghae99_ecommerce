package kr.hhplus.be.server.domain.point.dto;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import kr.hhplus.be.server.domain.point.dto.response.PointChargeServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.PointHistoryServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-17T15:01:22+0900",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class UserPointMapperImpl implements UserPointMapper {

    @Override
    public UserPointServiceResponse toUserPointResponse(UserPoint userPoint) {
        if ( userPoint == null ) {
            return null;
        }

        UserPointServiceResponse.UserPointServiceResponseBuilder userPointServiceResponse = UserPointServiceResponse.builder();

        userPointServiceResponse.userId( userPoint.getUserId() );
        userPointServiceResponse.point( userPoint.getPoint() );

        return userPointServiceResponse.build();
    }

    @Override
    public PointChargeServiceResponse toUserPointChargeResponse(UserPoint userPoint) {
        if ( userPoint == null ) {
            return null;
        }

        Long userId = null;
        Long point = null;

        userId = userPoint.getUserId();
        point = userPoint.getPoint();

        PointChargeServiceResponse pointChargeServiceResponse = new PointChargeServiceResponse( userId, point );

        return pointChargeServiceResponse;
    }

    @Override
    public PointHistoryServiceResponse toUserPointHistoryResponse(PointHistory pointHistory) {
        if ( pointHistory == null ) {
            return null;
        }

        Long userId = null;
        Long point = null;
        String type = null;
        String createdAt = null;

        userId = pointHistory.getUserId();
        point = pointHistory.getPoint();
        if ( pointHistory.getType() != null ) {
            type = pointHistory.getType().name();
        }
        createdAt = pointHistory.getCreatedAt();

        PointHistoryServiceResponse pointHistoryServiceResponse = new PointHistoryServiceResponse( userId, point, type, createdAt );

        return pointHistoryServiceResponse;
    }

    @Override
    public List<PointHistoryServiceResponse> toHistoryListResponse(List<PointHistory> pointHistories) {
        if ( pointHistories == null ) {
            return null;
        }

        List<PointHistoryServiceResponse> list = new ArrayList<PointHistoryServiceResponse>( pointHistories.size() );
        for ( PointHistory pointHistory : pointHistories ) {
            list.add( toUserPointHistoryResponse( pointHistory ) );
        }

        return list;
    }
}
