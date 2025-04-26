package kr.hhplus.be.server.common.scheduler;

import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class CouponExpireSchedulerTest {

    @Mock
    private CouponIssueJpaRepository couponIssueJpaRepository;

    @InjectMocks
    private CouponExpireScheduler couponExpireScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("성공: 만료된 쿠폰은 상태 2로 변경된다")
    void expireExpiredCoupons_success() {
        // given
        CouponIssue usableCoupon = CouponIssue.of(
                1L, 1L, 1L, 0,
                "2025-04-01T00:00:00", "2999-12-31T23:59:59", // 유효
                "2025-04-01T00:00:00", "2025-04-01T00:00:00"
        );

        CouponIssue expiredCoupon = CouponIssue.of(
                2L, 2L, 2L, 0,
                "2025-04-01T00:00:00", "2020-01-01T00:00:00", // 만료됨
                "2025-04-01T00:00:00", "2025-04-01T00:00:00"
        );

        when(couponIssueJpaRepository.findAll()).thenReturn(List.of(usableCoupon, expiredCoupon));

        // when
        couponExpireScheduler.runScheduler();

        // then
        verify(couponIssueJpaRepository, times(1)).findAll();
        assertThat(usableCoupon.getState()).isEqualTo(0);
        assertThat(expiredCoupon.getState()).isEqualTo(2);
    }
}
