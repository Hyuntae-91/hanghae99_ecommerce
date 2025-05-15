package kr.hhplus.be.server.domain.coupon.service.dto;


import kr.hhplus.be.server.domain.coupon.dto.response.IssueNewCouponServiceResponse;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssueNewCouponServiceResponseTest {

    @Test
    @DisplayName("성공: Coupon 으로부터 IssueNewCouponServiceResponse 생성")
    void createIssueNewCouponServiceResponse_fromCoupon() {
        // given
        Coupon coupon = Coupon.builder()
                .id(1L)
                .type(CouponType.FIXED)
                .description("1000원 할인")
                .discount(1000)
                .expirationDays(30)
                .state(1)
                .createdAt("2025-05-15T00:00:00")
                .updatedAt("2025-05-15T00:00:00")
                .build();

        // when
        IssueNewCouponServiceResponse response = IssueNewCouponServiceResponse.from(coupon);

        // then
        assertThat(response.couponId()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo("FIXED");
        assertThat(response.description()).isEqualTo("1000원 할인");
        assertThat(response.discount()).isEqualTo(1000);
    }
}
