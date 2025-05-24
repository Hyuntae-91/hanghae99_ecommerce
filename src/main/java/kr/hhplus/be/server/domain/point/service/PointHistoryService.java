package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.mapper.UserPointMapper;
import kr.hhplus.be.server.domain.point.dto.request.PointHistoryServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.PointHistoryServiceResponse;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserPointMapper userPointMapper;

    @Transactional
    public List<PointHistoryServiceResponse> getHistory(PointHistoryServiceRequest reqService) {
        UserPoint user = pointRepository.get(reqService.userId());
        return userPointMapper.toHistoryListResponse(
                pointHistoryRepository.getHistory(
                        reqService.userId(),
                        reqService.page(),
                        reqService.size(),
                        reqService.sort()
                )
        );
    }
}
