package kr.hhplus.be.server.domain.point.service.model;

import kr.hhplus.be.server.domain.point.model.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserPointTest {

    @Test
    @DisplayName("성공: 포인트를 정상적으로 충전할 수 있다")
    void chargePoint_success() {
        // given
        UserPoint userPoint = UserPoint.of(1L, 100L);

        // when
        userPoint.charge(200L);

        // then
        assertThat(userPoint.getPoint()).isEqualTo(300L);
    }

    @Test
    @DisplayName("성공: 포인트를 정상적으로 사용할 수 있다")
    void usePoint_success() {
        // given
        UserPoint userPoint = UserPoint.of(1L, 300L);

        // when
        userPoint.use(150L);

        // then
        assertThat(userPoint.getPoint()).isEqualTo(150L);
    }

    @Test
    @DisplayName("실패: 충전 금액이 0이면 예외 발생")
    void charge_zeroAmount() {
        UserPoint userPoint = UserPoint.of(1L, 100L);
        assertThatThrownBy(() -> userPoint.charge(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액");
    }

    @Test
    @DisplayName("실패: 사용 금액이 0이면 예외 발생")
    void use_zeroAmount() {
        UserPoint userPoint = UserPoint.of(1L, 100L);
        assertThatThrownBy(() -> userPoint.use(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 금액");
    }

    @Test
    @DisplayName("실패: 충전 금액이 0 이하인 경우 IllegalArgumentException 발생")
    void chargePoint_invalidAmount() {
        // given
        UserPoint userPoint = UserPoint.of(1L, 100L);

        // expect
        assertThatThrownBy(() -> userPoint.charge(-100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: 포인트 사용 시 금액이 0 이하이면 IllegalArgumentException 발생")
    void usePoint_invalidAmount() {
        // given
        UserPoint userPoint = UserPoint.of(1L, 100L);

        // expect
        assertThatThrownBy(() -> userPoint.use(-100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 금액은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: 포인트 사용 시 잔액보다 많은 금액을 사용하면 IllegalArgumentException 발생")
    void usePoint_insufficientBalance() {
        // given
        UserPoint userPoint = UserPoint.of(1L, 100L);

        // expect
        assertThatThrownBy(() -> userPoint.use(200L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잔액이 부족합니다.");
    }
}