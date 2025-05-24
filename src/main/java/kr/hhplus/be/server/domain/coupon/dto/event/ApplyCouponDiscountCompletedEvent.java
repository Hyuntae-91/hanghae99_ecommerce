package kr.hhplus.be.server.domain.coupon.dto.event;

import java.util.List;

public record ApplyCouponDiscountCompletedEvent(
        Long orderId,
        Long userId,
        Long couponId,
        Long finalPrice,
        List<Long> productIds
) {
    public ApplyCouponDiscountCompletedEvent {
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (couponId != null && couponId < 1) {
            throw new IllegalArgumentException("couponId는 1 이상이거나 null이어야 합니다.");
        }
        if (finalPrice == null || finalPrice < 0) {
            throw new IllegalArgumentException("finalPrice는 0보다 커야 합니다.");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("productIds는 null이거나 비어 있을 수 없습니다.");
        }
    }
}
