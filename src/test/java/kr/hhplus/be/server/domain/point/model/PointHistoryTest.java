package kr.hhplus.be.server.domain.point.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointHistoryTest {

    @Test
    @DisplayName("성공: PointHistory 객체 생성")
    void createPointHistory_success() {
        // given
        Long userId = 1L;
        Long point = 100L;
        PointHistoryType type = PointHistoryType.CHARGE;
        String createdAt = "2025-04-01T10:00:00";

        // when
        PointHistory pointHistory = new PointHistory(null, userId, point, type, createdAt);

        // then
        assertThat(pointHistory.getUserId()).isEqualTo(userId);
        assertThat(pointHistory.getPoint()).isEqualTo(point);
        assertThat(pointHistory.getType()).isEqualTo(type);
        assertThat(pointHistory.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("실패: type이 null이면 예외 발생")
    void createPointHistory_invalidType() {
        // given
        Long userId = 1L;
        Long point = 100L;
        String createdAt = "2025-04-01T10:00:00";

        // when & then
        assertThatThrownBy(() -> new PointHistory(null, userId, point, null, createdAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("type cannot be null");
    }

    @Test
    @DisplayName("실패: point가 음수이면 예외 발생")
    void createPointHistory_invalidPoint() {
        // given
        Long userId = 1L;
        String createdAt = "2025-04-01T10:00:00";

        // when & then
        assertThatThrownBy(() -> new PointHistory(null, userId, -1L, PointHistoryType.CHARGE, createdAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("point must be greater than or equal to 0");
    }

    @Test
    @DisplayName("실패: point가 음수일 경우 예외 발생")
    void createPointHistory_negativePoint() {
        assertThatThrownBy(() -> new PointHistory(null, 1L, -100L, PointHistoryType.CHARGE, "2024-04-07T00:00:00"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("point must be greater than or equal to 0");
    }


    @Test
    @DisplayName("성공: createdAt이 null이어도 생성 가능")
    void createPointHistory_createdAtNullable() {
        PointHistory history = new PointHistory(null, 1L, 100L, PointHistoryType.CHARGE, null);
        assertThat(history).isNotNull();
        assertThat(history.getCreatedAt()).isNull();
    }

}
