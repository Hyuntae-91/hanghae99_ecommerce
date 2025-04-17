package kr.hhplus.be.server.infrastructure.coupon.repository;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
