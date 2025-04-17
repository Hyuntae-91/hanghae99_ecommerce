package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.infrastructure.point.repository.PointHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public void saveHistory(Long userId, Long point, PointHistoryType historyType) {
        pointHistoryJpaRepository.save(PointHistory.of(userId, point, historyType));
    }

    @Override
    public List<PointHistory> getHistory(Long userId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort));
        return pointHistoryJpaRepository.findByUserId(userId, pageable);
    }
}
