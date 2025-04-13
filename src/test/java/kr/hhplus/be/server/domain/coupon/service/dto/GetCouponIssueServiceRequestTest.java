package kr.hhplus.be.server.domain.coupon.service.dto;

import kr.hhplus.be.server.domain.coupon.dto.GetCouponIssueServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCouponIssueServiceRequestTest {

    @Test
    @DisplayName("성공: couponIssueId가 1 이상일 때 객체 생성")
    void create_success() {
        GetCouponIssueServiceRequest request = new GetCouponIssueServiceRequest(1L);
        assertThat(request.couponIssueId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패: couponIssueId가 null인 경우 예외 발생")
    void fail_when_couponIssueId_null() {
        assertThatThrownBy(() -> new GetCouponIssueServiceRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponIssueId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: couponIssueId가 1 미만인 경우 예외 발생")
    void fail_when_couponIssueId_below_1() {
        assertThatThrownBy(() -> new GetCouponIssueServiceRequest(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponIssueId는 1 이상이어야 합니다.");
    }
}
