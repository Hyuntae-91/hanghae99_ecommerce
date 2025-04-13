package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.domain.common.exception.ResourceNotFoundException;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.infrastructure.point.repository.PointHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final UserPointJpaRepository userPointJpaRepository;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public UserPoint get(Long userId) {
        return userPointJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void savePoint(UserPoint userPoint) {
        userPointJpaRepository.save(userPoint);
    }

    @Override
    public void saveHistory(Long userId, Long point, PointHistoryType historyType) {
        PointHistory history = PointHistory.builder()
                .userId(userId)
                .point(point)
                .type(historyType)
                .build();
        pointHistoryJpaRepository.save(history);
    }

    @Override
    public List<PointHistory> getHistory(Long userId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort));
        return pointHistoryJpaRepository.findByUserId(userId, pageable);
    }
}
