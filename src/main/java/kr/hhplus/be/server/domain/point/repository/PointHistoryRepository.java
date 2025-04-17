package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;

import java.util.List;

public interface PointHistoryRepository {
    void saveHistory(Long userId, Long point, PointHistoryType historyType);
    List<PointHistory> getHistory(Long userId, int page, int size, String sort);
}
