package kr.hhplus.be.server.domain.order.service.model;

import kr.hhplus.be.server.domain.order.model.OrderOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderOptionTest {

    @Test
    @DisplayName("성공: 유효한 값으로 생성하면 예외가 발생하지 않는다")
    void validateFields_success() {
        // given
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(270)
                .stockQuantity(50)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        // when & then
        assertThatCode(orderOption::validateFields)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: size가 null이면 예외 발생")
    void validateFields_fail_when_size_is_null() {
        // given
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(null)
                .stockQuantity(50)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        // when & then
        assertThatThrownBy(orderOption::validateFields)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: size가 음수이면 예외 발생")
    void validateFields_fail_when_size_is_negative() {
        // given
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(-10)
                .stockQuantity(50)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        // when & then
        assertThatThrownBy(orderOption::validateFields)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: stockQuantity가 null이면 예외 발생")
    void validateFields_fail_when_stockQuantity_is_null() {
        // given
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(270)
                .stockQuantity(null)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        // when & then
        assertThatThrownBy(orderOption::validateFields)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("stockQuantity는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: stockQuantity가 음수이면 예외 발생")
    void validateFields_fail_when_stockQuantity_is_negative() {
        // given
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(270)
                .stockQuantity(-5)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        // when & then
        assertThatThrownBy(orderOption::validateFields)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("stockQuantity는 0 이상이어야 합니다.");
    }
}
