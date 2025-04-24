package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.dto.UserPointMapper;
import kr.hhplus.be.server.domain.point.dto.request.PointChargeServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.PointChargeServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.point.model.*;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserPointMapper userPointMapper;

    @Transactional(readOnly = true)
    public UserPointServiceResponse getUserPoint(UserPointServiceRequest reqService) {
        return userPointMapper.toUserPointResponse(pointRepository.get(reqService.userId()));
    }

    @Transactional
    public PointChargeServiceResponse charge(PointChargeServiceRequest reqService) {
        UserPoint userPoint = pointRepository.get(reqService.userId());
        userPoint.charge(reqService.point());
        pointRepository.savePoint(userPoint);
        pointHistoryRepository.saveHistory(reqService.userId(), reqService.point(), PointHistoryType.CHARGE);

        return userPointMapper.toUserPointChargeResponse(userPoint);
    }
}
