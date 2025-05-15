package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.model.Coupon;

import java.util.List;

public interface CouponRepository {
    Coupon findById(Long couponId);
    Coupon findWithLockById(Long couponId);
    Coupon save(Coupon coupon);
    List<Coupon> findActiveCoupons();
}
