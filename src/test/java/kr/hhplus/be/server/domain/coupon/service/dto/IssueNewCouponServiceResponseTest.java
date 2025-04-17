package kr.hhplus.be.server.domain.coupon.service.dto;


import kr.hhplus.be.server.domain.coupon.dto.response.IssueNewCouponServiceResponse;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssueNewCouponServiceResponseTest {

    @Test
    @DisplayName("성공: CouponIssue로부터 Response 생성")
    void from_success() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .type(CouponType.FIXED)
                .description("설명")
                .discount(1000)
                .quantity(10)
                .issued(1)
                .expirationDays(7)
                .createdAt("2025-04-11T10:00:00")
                .updatedAt("2025-04-11T10:00:00")
                .build();

        CouponIssue issue = CouponIssue.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .state(0)
                .startAt("2025-04-11T10:00:00")
                .endAt("2025-04-18T10:00:00")
                .createdAt("2025-04-11T10:00:00")
                .updatedAt("2025-04-11T10:00:00")
                .build();

        IssueNewCouponServiceResponse response = IssueNewCouponServiceResponse.from(issue, coupon);

        assertThat(response.couponId()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo("FIXED");
        assertThat(response.description()).isEqualTo("설명");
        assertThat(response.discount()).isEqualTo(1000);
        assertThat(response.state()).isEqualTo(0);
        assertThat(response.start_at()).isEqualTo("2025-04-11T10:00:00");
        assertThat(response.end_at()).isEqualTo("2025-04-18T10:00:00");
        assertThat(response.createdAt()).isEqualTo("2025-04-11T10:00:00");
    }
}
