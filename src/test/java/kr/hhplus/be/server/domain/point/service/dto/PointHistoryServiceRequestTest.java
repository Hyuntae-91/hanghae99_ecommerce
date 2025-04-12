package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.request.PointHistoryServiceRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointHistoryServiceRequestTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new PointHistoryServiceRequest(1L, 1, 10, "createdAt")).doesNotThrowAnyException();
    }

    @Test
    void nullUserId_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceRequest(null, 1, 10, "createdAt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pageLessThanOne_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceRequest(1L, 0, 10, "createdAt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sizeLessThanOne_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceRequest(1L, 1, 0, "createdAt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullSort_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceRequest(1L, 1, 10, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankSort_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceRequest(1L, 1, 10, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
