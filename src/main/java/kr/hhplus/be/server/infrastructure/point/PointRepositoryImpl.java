package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.domain.common.exception.ResourceNotFoundException;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.infrastructure.point.repository.PointHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
    public void saveHistory(PointHistory history) {
        pointHistoryJpaRepository.save(history);
    }

    @Override
    public List<PointHistory> getHistory(Long userId, Pageable pageable) {
        return pointHistoryJpaRepository.findByUserId(userId, pageable);
    }
}