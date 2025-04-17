package kr.hhplus.be.server.domain.order.service.model;

import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.exception.custom.ConflictException;
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

    @Test
    @DisplayName("성공: 재고가 충분한 경우 예외 발생하지 않음")
    void validateEnoughStock_success() {
        // given
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(270)
                .stockQuantity(10)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        // when & then
        assertThatCode(() -> orderOption.validateEnoughStock(5))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: 재고가 부족한 경우 예외 발생")
    void validateEnoughStock_fail_when_insufficient() {
        // given
        OrderOption orderOption = OrderOption.builder()
                .id(99L)
                .productId(1L)
                .size(270)
                .stockQuantity(3)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        // when & then
        assertThatThrownBy(() -> orderOption.validateEnoughStock(5))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("재고가 부족합니다. optionId=99");
    }

    @Test
    @DisplayName("성공: 재고 정상 차감")
    void decreaseStock_success() {
        // given
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(270)
                .stockQuantity(10)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        // when
        orderOption.decreaseStock(3);

        // then
        assertThat(orderOption.getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("실패: 차감 수량이 1 미만이면 예외 발생")
    void decreaseStock_fail_invalid_quantity() {
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(270)
                .stockQuantity(10)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        assertThatThrownBy(() -> orderOption.decreaseStock(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("차감 수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: 재고 부족으로 차감 실패")
    void decreaseStock_fail_when_insufficient_stock() {
        OrderOption orderOption = OrderOption.builder()
                .productId(1L)
                .size(270)
                .stockQuantity(2)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        assertThatThrownBy(() -> orderOption.decreaseStock(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

}
