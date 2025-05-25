package kr.hhplus.be.server.domain.coupon.dto.event;

public record ApplyCouponDiscountRequestedEvent(
        Long orderId,
        Long userId,
        Long couponId,
        Long totalPrice
) {
    public ApplyCouponDiscountRequestedEvent {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice는 0 이상이어야 합니다.");
        }
        if (orderId == null || orderId < 1) {
            throw new IllegalArgumentException("orderId는 1 이상이어야 합니다.");
        }
        // couponId는 null 또는 1 이상만 허용 (nullable 필드 처리)
        if (couponId != null && couponId < 1) {
            throw new IllegalArgumentException("couponId는 1 이상이거나 null이어야 합니다.");
        }
    }
}
