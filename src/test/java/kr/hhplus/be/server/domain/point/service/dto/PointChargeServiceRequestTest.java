package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.PointChargeServiceRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointChargeServiceRequestTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new PointChargeServiceRequest(1L, 100L)).doesNotThrowAnyException();
    }

    @Test
    void nullUserId_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceRequest(null, 100L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullPoint_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceRequest(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativePoint_throwsException() {
        assertThatThrownBy(() -> new PointChargeServiceRequest(1L, -100L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
