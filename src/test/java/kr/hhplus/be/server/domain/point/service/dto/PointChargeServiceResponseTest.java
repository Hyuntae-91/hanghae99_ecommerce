package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.response.PointChargeServiceResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointChargeServiceResponseTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new PointChargeServiceResponse(1L, 100L)).doesNotThrowAnyException();
    }

    @Test
    void nullPoint_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceResponse(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativePoint_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceResponse(1L, -100L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullUserId_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceResponse(null, 100L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void userIdLessThanOne_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceResponse(0L, 100L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
