package kr.hhplus.be.server.domain.point.service.dto;

import kr.hhplus.be.server.domain.point.dto.response.PointHistoryServiceResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointHistoryServiceResponseTest {

    @Test
    void validInput_doesNotThrow() {
        assertThatCode(() -> new PointHistoryServiceResponse(1L, 500L, "CHARGE", "2025-04-08T10:00:00"))
                .doesNotThrowAnyException();
    }

    @Test
    void nullUserId_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceResponse(null, 100L, "CHARGE", "2025-04-08"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullPoint_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceResponse(1L, null, "CHARGE", "2025-04-08"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullType_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceResponse(1L, 100L, null, "2025-04-08"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankType_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceResponse(1L, 100L, " ", "2025-04-08"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullCreatedAt_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceResponse(1L, 100L, "CHARGE", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankCreatedAt_throwsException() {
        assertThatThrownBy(() -> new PointHistoryServiceResponse(1L, 100L, "CHARGE", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
