package kr.hhplus.be.server.infrastructure.order.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderOptionJpaRepository extends JpaRepository<OrderOption, Long> {
    List<OrderOption> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderOption o WHERE o.id = :optionId")
    Optional<OrderOption> findWithLockById(@Param("optionId") Long optionId);
}