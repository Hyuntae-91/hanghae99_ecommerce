package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface PointRepository {
    UserPoint get(Long userId);
    void savePoint(UserPoint userPoint);
    void saveHistory(PointHistory history);
    List<PointHistory> getHistory(Long userId, Pageable pageable);
}
