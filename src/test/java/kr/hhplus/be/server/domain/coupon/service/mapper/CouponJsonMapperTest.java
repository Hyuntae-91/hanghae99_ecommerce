package kr.hhplus.be.server.domain.coupon.service.mapper;

import kr.hhplus.be.server.domain.coupon.dto.response.CouponDto;
import kr.hhplus.be.server.domain.coupon.mapper.CouponJsonMapper;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CouponJsonMapperTest {

    @Test
    @DisplayName("성공: Coupon 객체 → JSON 문자열 변환")
    void toCouponJson_success() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .type(CouponType.FIXED)
                .description("테스트 쿠폰")
                .discount(1000)
                .quantity(10)
                .state(1)
                .expirationDays(30)
                .createdAt("2024-01-01 00:00:00")
                .updatedAt("2024-01-01 00:00:00")
                .build();

        String json = CouponJsonMapper.toCouponJson(coupon);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"type\":\"FIXED\"");
        assertThat(json).contains("\"description\":\"테스트 쿠폰\"");
        assertThat(json).contains("\"discount\":1000");
    }

    @Test
    @DisplayName("성공: JSON 문자열 → Coupon 객체 변환")
    void fromCouponJson_success() {
        String json = """
            {
              "id": 2,
              "type": "PERCENT",
              "description": "퍼센트 쿠폰",
              "discount": 15,
              "quantity": 100,
              "state": 1,
              "expirationDays": 10,
              "createdAt": "2024-01-01 00:00:00",
              "updatedAt": "2024-01-01 00:00:00"
            }
            """;

        Coupon coupon = CouponJsonMapper.fromCouponJson(json);

        assertThat(coupon.getId()).isEqualTo(2L);
        assertThat(coupon.getType()).isEqualTo(CouponType.PERCENT);
        assertThat(coupon.getDescription()).isEqualTo("퍼센트 쿠폰");
        assertThat(coupon.getDiscount()).isEqualTo(15);
    }

    @Test
    @DisplayName("실패: 잘못된 JSON → 예외 발생")
    void fromCouponJson_invalidJson_shouldThrow() {
        String invalidJson = "{ 잘못된 JSON }";

        assertThatThrownBy(() -> CouponJsonMapper.fromCouponJson(invalidJson))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("JSON을 Coupon으로 변환할 수 없습니다.");
    }

    @Test
    @DisplayName("성공: Coupon JSON 문자열 리스트 → CouponDto 리스트 변환")
    void fromCouponJsonList_success() {
        String json1 = """
            {
              "id": 1,
              "type": "FIXED",
              "description": "A쿠폰",
              "discount": 500,
              "quantity": 100,
              "state": 1,
              "expirationDays": 7,
              "createdAt": "2024-01-01 00:00:00",
              "updatedAt": "2024-01-01 00:00:00"
            }
            """;

        String json2 = """
            {
              "id": 2,
              "type": "PERCENT",
              "description": "B쿠폰",
              "discount": 10,
              "quantity": 50,
              "state": 1,
              "expirationDays": 14,
              "createdAt": "2024-01-01 00:00:00",
              "updatedAt": "2024-01-01 00:00:00"
            }
            """;

        List<String> jsonList = List.of(json1, json2);

        List<CouponDto> result = CouponJsonMapper.fromCouponJsonList(jsonList);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).description()).isEqualTo("A쿠폰");
        assertThat(result.get(1).description()).isEqualTo("B쿠폰");
    }

    @Test
    @DisplayName("성공: null 또는 빈 리스트 입력 시 빈 리스트 반환")
    void fromCouponJsonList_nullOrEmpty() {
        assertThat(CouponJsonMapper.fromCouponJsonList(null)).isEmpty();
        assertThat(CouponJsonMapper.fromCouponJsonList(List.of())).isEmpty();
    }
}

