package kr.hhplus.be.server.domain.coupon.service.dto.request;

import kr.hhplus.be.server.domain.coupon.dto.request.ApplyCouponDiscountServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplyCouponDiscountServiceRequestTest {

    @Test
    @DisplayName("성공: ApplyCouponDiscountServiceRequest 정상 생성 (couponId 존재)")
    void createApplyCouponDiscountServiceRequest_success_withCoupon() {
        // given
        Long couponId = 5L;
        Long couponIssueId = 2L;
        Long userId = 1L;
        Long originalPrice = 10000L;

        // when
        ApplyCouponDiscountServiceRequest request = new ApplyCouponDiscountServiceRequest(
                couponId, couponIssueId, userId, originalPrice
        );

        // then
        assertThat(request.couponId()).isEqualTo(couponId);
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.originalPrice()).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("성공: ApplyCouponDiscountServiceRequest 정상 생성 (couponId 없음)")
    void createApplyCouponDiscountServiceRequest_success_withoutCoupon() {
        // given
        Long userId = 1L;
        Long originalPrice = 10000L;

        // when
        ApplyCouponDiscountServiceRequest request = new ApplyCouponDiscountServiceRequest(
                null, null, userId, originalPrice
        );

        // then
        assertThat(request.couponId()).isNull();
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.originalPrice()).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("예외: couponId가 1 미만이면 예외 발생")
    void createApplyCouponDiscountServiceRequest_invalidCouponId() {
        // given
        Long couponId = 0L;
        Long couponIssueId = 1L;
        Long userId = 1L;
        Long originalPrice = 10000L;

        // when, then
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(couponId, couponIssueId, userId, originalPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("예외: userId가 null이거나 1 미만이면 예외 발생")
    void createApplyCouponDiscountServiceRequest_invalidUserId() {
        // when, then
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(5L, 1L, null, 10000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");

        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(5L, 1L, 0L, 10000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("예외: originalPrice가 null이거나 0 미만이면 예외 발생")
    void createApplyCouponDiscountServiceRequest_invalidOriginalPrice() {
        // when, then
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(5L, 1L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("originalPrice는 0 이상이어야 합니다.");

        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(5L, 1L, 1L, -1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("originalPrice는 0 이상이어야 합니다.");
    }
}
