package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;

import java.util.List;


public interface PointRepository {
    UserPoint get(Long userId);
    void savePoint(UserPoint userPoint);
    void saveHistory(Long userId, Long point, PointHistoryType historyType);
    List<PointHistory> getHistory(Long userId, int page, int size, String sort);
}
