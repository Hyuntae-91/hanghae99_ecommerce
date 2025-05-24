package kr.hhplus.be.server.domain.point.service.dto.request;

import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserPointServiceRequestTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new UserPointServiceRequest(1L)).doesNotThrowAnyException();
    }

    @Test
    void nullUserId_throwsException() {
        assertThatThrownBy(() -> new UserPointServiceRequest(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidUserId_throwsException() {
        assertThatThrownBy(() -> new UserPointServiceRequest(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
