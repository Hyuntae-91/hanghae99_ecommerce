package kr.hhplus.be.server.domain.coupon.service.dto.response;

import kr.hhplus.be.server.domain.coupon.dto.response.ApplyCouponDiscountServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplyCouponDiscountServiceResponseTest {

    @Test
    @DisplayName("성공: 정상 finalPrice 입력")
    void success_create_response() {
        ApplyCouponDiscountServiceResponse response = new ApplyCouponDiscountServiceResponse(3000L);
        assertThat(response.finalPrice()).isEqualTo(3000L);
    }

    @Test
    @DisplayName("실패: finalPrice가 null인 경우")
    void fail_when_finalPrice_is_null() {
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: finalPrice가 음수인 경우")
    void fail_when_finalPrice_is_negative() {
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceResponse(-1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finalPrice는 0 이상이어야 합니다.");
    }
}
