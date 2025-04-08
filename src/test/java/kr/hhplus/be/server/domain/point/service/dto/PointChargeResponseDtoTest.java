package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.PointChargeResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointChargeResponseDtoTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new PointChargeResponseDto(100L)).doesNotThrowAnyException();
    }

    @Test
    void nullPoint_throwsException() {
        assertThatThrownBy(() -> new PointChargeResponseDto(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativePoint_throwsException() {
        assertThatThrownBy(() -> new PointChargeResponseDto(-100L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
