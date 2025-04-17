package kr.hhplus.be.server.domain.coupon.service.model;

import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.coupon.dto.request.IssueNewCouponServiceRequest;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.exception.custom.InvalidCouponUseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponIssueRepository couponIssueRepository;

    @InjectMocks
    private CouponService couponService;

    private static String now() {
        return java.time.LocalDateTime.now().toString();
    }

    @Test
    @DisplayName("성공: 쿠폰 발급 수량 증가")
    void increase_issued_success() {
        // given
        Coupon coupon = Coupon.builder()
                .id(1L)
                .type(CouponType.FIXED)
                .description("테스트 쿠폰")
                .discount(1000)
                .quantity(10)
                .issued(5)
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build();

        // when
        coupon.increaseIssued();

        // then
        assertThat(coupon.getIssued()).isEqualTo(6);
        assertThat(coupon.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("실패: 쿠폰 발급 수량 초과")
    void increase_issued_fail_when_exceed_quantity() {
        // given
        Coupon coupon = Coupon.builder()
                .id(1L)
                .type(CouponType.FIXED)
                .description("테스트 쿠폰")
                .discount(1000)
                .quantity(5)
                .issued(5) // 이미 최대 수량
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build();

        // then
        assertThatThrownBy(coupon::increaseIssued)
                .isInstanceOf(InvalidCouponUseException.class)
                .hasMessageContaining("쿠폰 발급 수량을 초과했습니다.");
    }

    @Test
    @DisplayName("실패: 쿠폰 발급 수량 초과 시 예외 발생")
    void issue_new_coupon_fail_when_exceeds_quantity() {
        // given
        Coupon coupon = Coupon.builder()
                .id(1L)
                .type(CouponType.FIXED)
                .description("테스트 쿠폰")
                .discount(1000)
                .quantity(5)
                .issued(5) // 초과 상태
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build();

        when(couponRepository.findById(1L)).thenReturn(coupon);

        // then
        assertThatThrownBy(() ->
                couponService.issueNewCoupon(new IssueNewCouponServiceRequest(1L, 1L)))
                .isInstanceOf(InvalidCouponUseException.class)
                .hasMessageContaining("쿠폰 발급 수량을 초과했습니다.");
    }
}
