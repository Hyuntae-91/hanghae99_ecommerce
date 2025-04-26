package kr.hhplus.be.server.domain.coupon.service.dto;

import kr.hhplus.be.server.domain.coupon.dto.request.ApplyCouponDiscountServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class ApplyCouponDiscountServiceRequestTest {

    @Test
    @DisplayName("성공: 정상 입력")
    void success_create_request() {
        ApplyCouponDiscountServiceRequest request = new ApplyCouponDiscountServiceRequest(1L, 5000L);
        assertThat(request.couponIssueId()).isEqualTo(1L);
        assertThat(request.originalPrice()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("성공: couponIssueId가 null인 경우")
    void success_when_couponIssueId_is_null() {
        ApplyCouponDiscountServiceRequest request = new ApplyCouponDiscountServiceRequest(null, 5000L);
        assertThat(request.couponIssueId()).isNull();
        assertThat(request.originalPrice()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("실패: couponIssueId가 0 이하인 경우")
    void fail_when_couponIssueId_is_invalid() {
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(0L, 5000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponIssueId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: originalPrice가 null인 경우")
    void fail_when_originalPrice_is_null() {
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("originalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: originalPrice가 음수인 경우")
    void fail_when_originalPrice_is_negative() {
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(1L, -1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("originalPrice는 0 이상이어야 합니다.");
    }
}
