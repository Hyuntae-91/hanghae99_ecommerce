package kr.hhplus.be.server.domain.coupon.dto.event;

import java.util.List;

public record ApplyCouponDiscountCompletedEvent(
        Long orderId,
        Long userId,
        Long couponId,
        Long finalPrice,
        List<Long> productIds
) {}
