package kr.hhplus.be.server.domain.coupon.service.dto;

import kr.hhplus.be.server.domain.coupon.dto.response.CouponIssueDto;
import kr.hhplus.be.server.domain.coupon.dto.response.GetCouponsServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCouponsServiceResponseTest {

    @Test
    @DisplayName("성공: 쿠폰 리스트가 존재할 경우 객체 생성")
    void create_success() {
        CouponIssueDto dto = new CouponIssueDto(
                1L,
                "FIXED",
                "테스트 쿠폰",
                1000,
                0,
                "2025-04-01T00:00:00",
                "2025-04-10T00:00:00",
                "2025-04-01T00:00:00"
        );

        GetCouponsServiceResponse response = new GetCouponsServiceResponse(List.of(dto));
        assertThat(response.coupons()).hasSize(1);
    }

    @Test
    @DisplayName("실패: 쿠폰 리스트가 null일 경우 예외 발생")
    void fail_when_coupons_null() {
        assertThatThrownBy(() -> new GetCouponsServiceResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유한 쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("실패: 쿠폰 리스트가 비어 있을 경우 예외 발생")
    void fail_when_coupons_empty() {
        assertThatThrownBy(() -> new GetCouponsServiceResponse(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유한 쿠폰이 없습니다.");
    }
}
