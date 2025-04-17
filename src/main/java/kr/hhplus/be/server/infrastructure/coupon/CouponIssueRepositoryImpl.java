package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponIssueRepositoryImpl implements CouponIssueRepository {

    private final CouponIssueJpaRepository couponIssueJpaRepository;

    @Override
    public List<CouponIssue> findByUserId(Long userId) {
        return couponIssueJpaRepository.findAllByUserId(userId);
    }

    @Override
    public List<CouponIssue> findUsableByUserId(Long userId) {
        String now = LocalDateTime.now().toString();
        return couponIssueJpaRepository.findAllByUserIdAndStateAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                userId, 0, now, now
        );
    }

    @Override
    public CouponIssue findById(Long couponIssueId) {
        return couponIssueJpaRepository.findById(couponIssueId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 쿠폰 발급 정보입니다."));
    }

    @Override
    public CouponIssue save(CouponIssue couponIssue) {
        return couponIssueJpaRepository.save(couponIssue);
    }
}
