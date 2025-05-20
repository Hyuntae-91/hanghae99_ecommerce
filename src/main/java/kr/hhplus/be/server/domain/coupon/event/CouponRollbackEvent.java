package kr.hhplus.be.server.domain.coupon.event;

public record CouponRollbackEvent(Long userId, Long couponId) {
}