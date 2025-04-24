package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final UserPointJpaRepository userPointJpaRepository;

    @Override
    public UserPoint get(Long userId) {
        return userPointJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserPoint findWithLockByUserId(Long userId) {
        return userPointJpaRepository.findWithLockByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void savePoint(UserPoint userPoint) { userPointJpaRepository.save(userPoint); }

}
