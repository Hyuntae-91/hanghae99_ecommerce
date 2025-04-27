package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.request.PointUseServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointUseServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 값으로 PointUseServiceRequest 생성")
    void create_valid_request() {
        PointUseServiceRequest request = new PointUseServiceRequest(1L, 500L);

        assertThat(request.userId()).isEqualTo(1L);
        assertThat(request.point()).isEqualTo(500L);
    }

    @Test
    @DisplayName("실패: userId가 null인 경우 예외 발생")
    void fail_when_userId_is_null() {
        assertThatThrownBy(() -> new PointUseServiceRequest(null, 500L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만인 경우 예외 발생")
    void fail_when_userId_less_than_1() {
        assertThatThrownBy(() -> new PointUseServiceRequest(0L, 500L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: point가 null인 경우 예외 발생")
    void fail_when_point_is_null() {
        assertThatThrownBy(() -> new PointUseServiceRequest(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 포인트는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("실패: point가 0 이하인 경우 예외 발생")
    void fail_when_point_is_zero_or_negative() {
        assertThatThrownBy(() -> new PointUseServiceRequest(1L, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 포인트는 0보다 커야 합니다.");

        assertThatThrownBy(() -> new PointUseServiceRequest(1L, -100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 포인트는 0보다 커야 합니다.");
    }
}
