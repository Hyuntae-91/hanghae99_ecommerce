package kr.hhplus.be.server.domain.coupon.service.model;

import kr.hhplus.be.server.domain.coupon.dto.CouponIssueDto;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CouponIssueTest {
    @Test
    @DisplayName("성공: FIXED 쿠폰의 할인 계산")
    void calculateFinalPrice_fixed() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 0, 7, "2024-04-11T00:00:00", "2024-04-11T00:00:00");
        CouponIssue issue = new CouponIssue(1L, 1L, coupon, 0, "2024-01-01T00:00:00", "2099-01-01T00:00:00", "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        long result = issue.calculateFinalPrice(5000L);
        assertThat(result).isEqualTo(4000L);
    }

    @Test
    @DisplayName("성공: PERCENT 쿠폰의 할인 계산")
    void calculateFinalPrice_percent() {
        Coupon coupon = new Coupon(2L, CouponType.PERCENT, "10% 할인", 10, 10, 0, 7, "2024-04-11T00:00:00", "2024-04-11T00:00:00");
        CouponIssue issue = new CouponIssue(1L, 1L, coupon, 0, "2024-01-01T00:00:00", "2099-01-01T00:00:00", "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        long result = issue.calculateFinalPrice(5000L);
        assertThat(result).isEqualTo(4500L);
    }

    @Test
    @DisplayName("성공: 할인액이 원금보다 클 경우 최대 할인은 원금까지")
    void calculateFinalPrice_cannot_exceed_original_price() {
        Coupon coupon = new Coupon(3L, CouponType.FIXED, "Big 할인", 99999, 10, 0, 7, "2024-04-11T00:00:00", "2024-04-11T00:00:00");
        CouponIssue issue = new CouponIssue(1L, 1L, coupon, 0, "2024-01-01T00:00:00", "2099-01-01T00:00:00", "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        long result = issue.calculateFinalPrice(3000L);
        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("성공: 사용 가능 상태 검증 통과")
    void validateUsable_success() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 0, 7, "2024-04-11T00:00:00", "2024-04-11T00:00:00");
        CouponIssue issue = new CouponIssue(1L, 1L, coupon, 0, "2024-01-01T00:00:00", "2099-01-01T00:00:00", "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        assertThatCode(issue::validateUsable).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: 쿠폰 상태가 0이 아닌 경우 예외 발생")
    void validateUsable_invalid_state() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 0, 7, "2024-04-11T00:00:00", "2024-04-11T00:00:00");
        CouponIssue issue = new CouponIssue(1L, 1L, coupon, 1, "2024-01-01T00:00:00", "2099-01-01T00:00:00", "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        assertThatThrownBy(issue::validateUsable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("사용할 수 없는 쿠폰");
    }

    @Test
    @DisplayName("성공: 쿠폰 사용 처리")
    void markUsed_success() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 0, 7, "2024-04-11T00:00:00", "2024-04-11T00:00:00");
        CouponIssue issue = new CouponIssue(1L, 1L, coupon, 0, "2024-01-01T00:00:00", "2099-01-01T00:00:00", "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        issue.markUsed();
        assertThat(issue.getState()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공: 쿠폰 상태 업데이트")
    void updateState_success() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 0, 7, "2024-04-11T00:00:00", "2024-04-11T00:00:00");
        CouponIssue issue = new CouponIssue(1L, 1L, coupon, 0, "2024-01-01T00:00:00", "2099-01-01T00:00:00", "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        issue.updateState(-1);
        assertThat(issue.getState()).isEqualTo(-1);
    }

    @Test
    @DisplayName("성공: toDto 동작 확인")
    void toDto_success() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 0, 7, "2024-04-11T00:00:00", "2024-04-11T00:00:00");
        CouponIssue issue = new CouponIssue(1L, 1L, coupon, 0, "2024-01-01T00:00:00", "2099-01-01T00:00:00", "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        CouponIssueDto dto = issue.toDto();
        assertThat(dto).isNotNull();
        assertThat(dto.couponId()).isEqualTo(1L);
        assertThat(dto.discount()).isEqualTo(1000);
    }

}
