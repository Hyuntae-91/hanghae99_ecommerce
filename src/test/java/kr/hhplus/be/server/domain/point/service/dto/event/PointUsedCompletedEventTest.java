package kr.hhplus.be.server.domain.point.service.dto.event;

import kr.hhplus.be.server.domain.point.dto.event.PointUsedCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class PointUsedCompletedEventTest {

    @Test
    @DisplayName("성공: 모든 필드가 유효하면 객체 생성")
    void success_create_event() {
        PointUsedCompletedEvent event = new PointUsedCompletedEvent(
                1L,
                10L,
                100L,
                List.of(1L, 2L)
        );

        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.userId()).isEqualTo(10L);
        assertThat(event.usedPoint()).isEqualTo(100L);
        assertThat(event.productIds()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("실패: orderId가 null이면 예외 발생")
    void fail_when_orderId_null() {
        assertThatThrownBy(() ->
                new PointUsedCompletedEvent(null, 1L, 100L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: orderId가 1 미만이면 예외 발생")
    void fail_when_orderId_invalid() {
        assertThatThrownBy(() ->
                new PointUsedCompletedEvent(0L, 1L, 100L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 null이면 예외 발생")
    void fail_when_userId_null() {
        assertThatThrownBy(() ->
                new PointUsedCompletedEvent(1L, null, 100L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만이면 예외 발생")
    void fail_when_userId_invalid() {
        assertThatThrownBy(() ->
                new PointUsedCompletedEvent(1L, 0L, 100L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: usedPoint가 null이면 예외 발생")
    void fail_when_usedPoint_null() {
        assertThatThrownBy(() ->
                new PointUsedCompletedEvent(1L, 1L, null, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("usedPoint는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: usedPoint가 0 미만이면 예외 발생")
    void fail_when_usedPoint_negative() {
        assertThatThrownBy(() ->
                new PointUsedCompletedEvent(1L, 1L, -10L, List.of(1L))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("usedPoint는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: productIds가 null이면 예외 발생")
    void fail_when_productIds_null() {
        assertThatThrownBy(() ->
                new PointUsedCompletedEvent(1L, 1L, 100L, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productIds는 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: productIds가 빈 리스트면 예외 발생")
    void fail_when_productIds_empty() {
        assertThatThrownBy(() ->
                new PointUsedCompletedEvent(1L, 1L, 100L, List.of())
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productIds는 null이거나 비어 있을 수 없습니다.");
    }
}

