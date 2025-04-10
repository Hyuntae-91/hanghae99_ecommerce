package kr.hhplus.be.server.infrastructure.point.repository;

import kr.hhplus.be.server.domain.point.model.PointHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByUserId(Long userId, Pageable pageable);
}
