package kr.hhplus.be.server.domain.point.service.dto.response;

import kr.hhplus.be.server.domain.point.dto.response.PointUseServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointUseServiceResponseTest {

    @Test
    @DisplayName("성공: 유효한 값으로 PointUseServiceResponse 생성")
    void create_valid_response() {
        PointUseServiceResponse response = new PointUseServiceResponse(1L, 500L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.point()).isEqualTo(500L);
    }

    @Test
    @DisplayName("성공: point가 0이어도 생성 가능")
    void create_with_zero_point() {
        PointUseServiceResponse response = new PointUseServiceResponse(1L, 0L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.point()).isEqualTo(0L);
    }

    @Test
    @DisplayName("실패: userId가 null인 경우 예외 발생")
    void fail_when_userId_is_null() {
        assertThatThrownBy(() -> new PointUseServiceResponse(null, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만인 경우 예외 발생")
    void fail_when_userId_less_than_1() {
        assertThatThrownBy(() -> new PointUseServiceResponse(0L, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: point가 null인 경우 예외 발생")
    void fail_when_point_is_null() {
        assertThatThrownBy(() -> new PointUseServiceResponse(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("point는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: point가 0 미만인 경우 예외 발생")
    void fail_when_point_less_than_0() {
        assertThatThrownBy(() -> new PointUseServiceResponse(1L, -1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("point는 0 이상이어야 합니다.");
    }
}