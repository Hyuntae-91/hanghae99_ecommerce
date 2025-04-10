package kr.hhplus.be.server.domain.point;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.point.dto.*;
import kr.hhplus.be.server.domain.point.model.*;
import kr.hhplus.be.server.infrastructure.point.dto.GetHistoryRepositoryRequestDto;
import kr.hhplus.be.server.infrastructure.point.dto.GetPointRepositoryRequestDto;
import kr.hhplus.be.server.infrastructure.point.dto.SavePointHistoryRepoRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    public UserPointServiceResponse getUserPoint(UserPointServiceRequest reqService) {
        GetPointRepositoryRequestDto reqRepository = new GetPointRepositoryRequestDto(reqService.userId());
        UserPoint userPoint = pointRepository.get(reqRepository);
        return UserPointServiceResponse.from(userPoint);
    }

    public List<PointHistoryServiceResponse> getHistory(PointHistoryServiceRequest reqService) {
        GetHistoryRepositoryRequestDto reqRepository = new GetHistoryRepositoryRequestDto(
                reqService.userId(), reqService.page(), reqService.size(), reqService.sort()
        );
        List<PointHistory> historyList = pointRepository.getHistory(reqRepository);

        return historyList.stream()
            .map(PointHistoryServiceResponse::from)
            .toList();
    }

    @Transactional
    public PointChargeServiceResponse charge(PointChargeServiceRequest reqService) {
        GetPointRepositoryRequestDto requestDto = new GetPointRepositoryRequestDto(reqService.userId());
        UserPoint userPoint = pointRepository.get(requestDto);
        userPoint.charge(reqService.point());
        pointRepository.savePoint(userPoint);

        SavePointHistoryRepoRequestDto reqRepository = new SavePointHistoryRepoRequestDto(
                reqService.userId(),
                reqService.point(),
                PointHistoryType.CHARGE
        );
        pointRepository.saveHistory(reqRepository);

        return new PointChargeServiceResponse(userPoint.getPoint());
    }
}
