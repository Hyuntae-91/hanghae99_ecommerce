package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.infrastructure.point.dto.GetHistoryRepositoryRequestDto;
import kr.hhplus.be.server.infrastructure.point.dto.GetPointRepositoryRequestDto;
import kr.hhplus.be.server.infrastructure.point.dto.SavePointHistoryRepoRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface PointRepository {
    UserPoint get(GetPointRepositoryRequestDto getPointRepositoryRequestDto);
    void savePoint(UserPoint userPoint);
    void saveHistory(SavePointHistoryRepoRequestDto reqRepository);
    List<PointHistory> getHistory(GetHistoryRepositoryRequestDto repositoryRequest);
}
