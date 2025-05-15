package kr.hhplus.be.server.domain.coupon.service.dto;

import kr.hhplus.be.server.domain.coupon.dto.response.CouponDto;
import kr.hhplus.be.server.domain.coupon.dto.response.GetCouponsServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCouponsServiceResponseTest {

    @Test
    @DisplayName("성공: GetCouponsServiceResponse 정상 생성")
    void createGetCouponsServiceResponse_success() {
        // given
        List<CouponDto> couponList = List.of(
                new CouponDto(1L, "FIXED", "1000원 할인", 1000, 30),
                new CouponDto(2L, "PERCENT", "10% 할인", 10, 30)
        );

        // when
        GetCouponsServiceResponse response = new GetCouponsServiceResponse(couponList);

        // then
        assertThat(response.coupons()).hasSize(2);
        assertThat(response.coupons()).extracting(CouponDto::id)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("예외: 쿠폰 리스트가 null이면 예외 발생")
    void createGetCouponsServiceResponse_nullCoupons() {
        // when, then
        assertThatThrownBy(() -> new GetCouponsServiceResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유한 쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("예외: 쿠폰 리스트가 비어있으면 예외 발생")
    void createGetCouponsServiceResponse_emptyCoupons() {
        // when, then
        assertThatThrownBy(() -> new GetCouponsServiceResponse(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유한 쿠폰이 없습니다.");
    }
}
