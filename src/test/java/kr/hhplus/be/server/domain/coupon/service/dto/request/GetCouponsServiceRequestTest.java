package kr.hhplus.be.server.domain.coupon.service.dto.request;

import kr.hhplus.be.server.domain.coupon.dto.request.GetCouponsServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCouponsServiceRequestTest {

    @Test
    @DisplayName("성공: userId가 1 이상일 때 객체 생성")
    void create_success() {
        GetCouponsServiceRequest request = new GetCouponsServiceRequest(1L);
        assertThat(request.userId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패: userId가 null인 경우 예외 발생")
    void fail_when_userId_null() {
        assertThatThrownBy(() -> new GetCouponsServiceRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만인 경우 예외 발생")
    void fail_when_userId_below_1() {
        assertThatThrownBy(() -> new GetCouponsServiceRequest(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }
}
