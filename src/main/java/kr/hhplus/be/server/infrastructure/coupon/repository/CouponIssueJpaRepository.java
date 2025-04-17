package kr.hhplus.be.server.infrastructure.coupon.repository;

import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponIssueJpaRepository extends JpaRepository<CouponIssue, Long> {
    List<CouponIssue> findAllByUserId(Long userId);
    List<CouponIssue> findAllByUserIdAndStateAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            Long userId, Integer state, String startAt, String endAt
    );
}