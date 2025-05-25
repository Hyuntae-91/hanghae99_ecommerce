package kr.hhplus.be.server.domain.coupon.dto.event;

public record CouponRollbackEvent(Long userId, Long couponId) {
}