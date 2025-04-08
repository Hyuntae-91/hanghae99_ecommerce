package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.UserPointRequestDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserPointRequestDtoTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new UserPointRequestDto(1L)).doesNotThrowAnyException();
    }

    @Test
    void nullUserId_throwsException() {
        assertThatThrownBy(() -> new UserPointRequestDto(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidUserId_throwsException() {
        assertThatThrownBy(() -> new UserPointRequestDto(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
