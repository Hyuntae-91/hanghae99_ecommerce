package kr.hhplus.be.server.domain.coupon.service.dto.request;

import kr.hhplus.be.server.domain.coupon.dto.request.SaveCouponStateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class SaveCouponStateRequestTest {

    @Test
    @DisplayName("성공: 유효한 couponIssueId와 state로 생성")
    void create_valid_request() {
        SaveCouponStateRequest request = new SaveCouponStateRequest(1L, 0);
        assertThat(request.couponIssueId()).isEqualTo(1L);
        assertThat(request.state()).isEqualTo(0);
    }

    @Test
    @DisplayName("실패: couponIssueId가 null인 경우")
    void fail_when_couponIssueId_is_null() {
        assertThatThrownBy(() -> new SaveCouponStateRequest(null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponIssueId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: couponIssueId가 1 미만인 경우")
    void fail_when_couponIssueId_is_less_than_1() {
        assertThatThrownBy(() -> new SaveCouponStateRequest(0L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponIssueId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: state가 -2인 경우")
    void fail_when_state_is_less_than_minus1() {
        assertThatThrownBy(() -> new SaveCouponStateRequest(1L, -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("state 는 -1 이상이어야 합니다.");
    }
}
