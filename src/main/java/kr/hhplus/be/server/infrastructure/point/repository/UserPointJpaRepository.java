package kr.hhplus.be.server.infrastructure.point.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPointJpaRepository extends JpaRepository<UserPoint, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserPoint> findWithLockByUserId(@Param("id") Long userId);
}