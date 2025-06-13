package kr.hhplus.be.server.domain.coupon.service.dto.response;

import kr.hhplus.be.server.domain.coupon.dto.response.CouponDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponDtoTest {

    @Test
    @DisplayName("성공: CouponDto 정상 생성")
    void createCouponDto_success() {
        // given
        Long id = 1L;
        Long issueId = 1L;
        String type = "FIXED";
        String description = "1000원 할인";
        Integer discount = 1000;
        Integer expirationDays = 30;

        // when
        CouponDto dto = new CouponDto(id, issueId, type, description, discount, expirationDays);

        // then
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.type()).isEqualTo(type);
        assertThat(dto.description()).isEqualTo(description);
        assertThat(dto.discount()).isEqualTo(discount);
        assertThat(dto.expirationDays()).isEqualTo(expirationDays);
    }

    @Test
    @DisplayName("예외: id가 null 또는 1 미만이면 예외 발생")
    void createCouponDto_invalidId() {
        assertThatThrownBy(() -> new CouponDto(null, 1L,"FIXED", "할인", 1000, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id는 1 이상이어야 합니다.");

        assertThatThrownBy(() -> new CouponDto(0L, null,"FIXED", "할인", 1000, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("예외: type이 null 또는 빈 값이면 예외 발생")
    void createCouponDto_invalidType() {
        assertThatThrownBy(() -> new CouponDto(1L, 1L, "할인", "test", 1000, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type은 필수입니다.");

        assertThatThrownBy(() -> new CouponDto(1L, 1L, "", "할인", 1000, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type은 필수입니다.");
    }

    @Test
    @DisplayName("예외: discount가 null 또는 0 미만이면 예외 발생")
    void createCouponDto_invalidDiscount() {
        assertThatThrownBy(() -> new CouponDto(1L, 1L,"FIXED", "할인", null, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("discount는 0 이상이어야 합니다.");

        assertThatThrownBy(() -> new CouponDto(1L, 1L, "FIXED", "할인", -10, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("discount는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("예외: expirationDays가 null 또는 1 미만이면 예외 발생")
    void createCouponDto_invalidExpirationDays() {
        assertThatThrownBy(() -> new CouponDto(1L, 1L,"FIXED", "할인", 1000, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirationDays는 1 이상이어야 합니다.");

        assertThatThrownBy(() -> new CouponDto(1L, 1L,"FIXED", "할인", 1000, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirationDays는 1 이상이어야 합니다.");
    }
}
