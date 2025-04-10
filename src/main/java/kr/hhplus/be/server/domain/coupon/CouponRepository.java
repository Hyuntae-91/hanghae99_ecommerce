package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.model.Coupon;

public interface CouponRepository {
    Coupon findById(Long couponId);
    Coupon save(Coupon coupon);
}
