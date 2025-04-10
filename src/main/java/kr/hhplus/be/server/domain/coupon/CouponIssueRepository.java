package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.model.CouponIssue;

import java.util.List;

public interface CouponIssueRepository {
    CouponIssue findById(Long couponIssueId);
    CouponIssue save(CouponIssue couponIssue);
    List<CouponIssue> findByUserId(Long userId);
}
