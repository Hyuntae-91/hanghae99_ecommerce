package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.UserPointServiceResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserPointServiceResponseTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new UserPointServiceResponse(1L, 1000L)).doesNotThrowAnyException();
    }

    @Test
    void nullUserId_throwsException() {
        assertThatThrownBy(() -> new UserPointServiceResponse(null, 1000L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullPoint_throwsException() {
        assertThatThrownBy(() -> new UserPointServiceResponse(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativePoint_throwsException() {
        assertThatThrownBy(() -> new UserPointServiceResponse(1L, -100L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
