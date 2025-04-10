package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.PointChargeServiceResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointChargeServiceResponseTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new PointChargeServiceResponse(100L)).doesNotThrowAnyException();
    }

    @Test
    void nullPoint_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceResponse(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativePoint_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceResponse(-100L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
