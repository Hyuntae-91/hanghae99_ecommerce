package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponIssueRepositoryImpl implements CouponIssueRepository {

    @Override
    public List<CouponIssue> findByUserId(Long userId) {
        return null;
    }

    @Override
    public CouponIssue findById(Long couponIssueId) { return null; }

    @Override
    public CouponIssue save(CouponIssue couponIssue) { return null;}
}
