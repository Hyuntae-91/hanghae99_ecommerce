package kr.hhplus.be.server.domain.coupon.service.dto;


import kr.hhplus.be.server.domain.coupon.dto.request.IssueNewCouponServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IssueNewCouponServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 값으로 객체 생성")
    void create_success() {
        IssueNewCouponServiceRequest request = new IssueNewCouponServiceRequest(1L, 2L);

        assertThat(request.userId()).isEqualTo(1L);
        assertThat(request.couponId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("실패: userId가 null일 경우")
    void fail_when_userId_null() {
        assertThatThrownBy(() -> new IssueNewCouponServiceRequest(null, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만일 경우")
    void fail_when_userId_less_than_1() {
        assertThatThrownBy(() -> new IssueNewCouponServiceRequest(0L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: couponId가 null일 경우")
    void fail_when_couponId_null() {
        assertThatThrownBy(() -> new IssueNewCouponServiceRequest(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: couponId가 1 미만일 경우")
    void fail_when_couponId_less_than_1() {
        assertThatThrownBy(() -> new IssueNewCouponServiceRequest(1L, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponId는 1 이상이어야 합니다.");
    }
}
