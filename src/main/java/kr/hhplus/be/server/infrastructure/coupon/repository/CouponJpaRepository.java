package kr.hhplus.be.server.infrastructure.coupon.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Coupon> findWithLockById(@Param("couponId") Long couponId);
}
