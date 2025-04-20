package kr.hhplus.be.server.domain.coupon.service.dto;

import kr.hhplus.be.server.domain.coupon.dto.response.CouponIssueDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponIssueDtoTest {

    private String nowStr() {
        return now().toString();
    }

    @Test
    @DisplayName("성공: 모든 필드 유효한 경우 생성")
    void create_success() {
        CouponIssueDto dto = new CouponIssueDto(1L, "FIXED", "desc", 1000, 0, nowStr(), nowStr(), nowStr());
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("실패: couponId가 null")
    void fail_couponId_null() {
        assertThatThrownBy(() -> new CouponIssueDto(null, "FIXED", "desc", 1000, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: couponId가 1 미만")
    void fail_couponId_below_1() {
        assertThatThrownBy(() -> new CouponIssueDto(0L, "FIXED", "desc", 1000, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("couponId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: type이 null")
    void fail_type_null() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, null, "desc", 1000, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: type이 빈 문자열")
    void fail_type_blank() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, " ", "desc", 1000, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: type이 FIXED/PERCENT가 아닌 경우")
    void fail_type_invalid_value() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "INVALID", "desc", 1000, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type은 PERCENT 또는 FIXED 이어야 합니다.");
    }

    @Test
    @DisplayName("실패: description이 null")
    void fail_description_null() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", null, 1000, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("description은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: description이 빈 문자열")
    void fail_description_blank() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", " ", 1000, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("description은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: discount가 null")
    void fail_discount_null() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", null, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("discount는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: discount가 0 미만")
    void fail_discount_negative() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", -10, 0, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("discount는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("실패: state가 null")
    void fail_state_null() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", 1000, null, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("state는 0, 1, -1 중 하나여야 합니다.");
    }

    @Test
    @DisplayName("실패: state가 허용되지 않은 값")
    void fail_state_invalid() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", 1000, 99, nowStr(), nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("state는 0, 1, -1 중 하나여야 합니다.");
    }

    @Test
    @DisplayName("실패: startAt이 null")
    void fail_startAt_null() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", 1000, 0, null, nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startAt은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: startAt이 빈 문자열")
    void fail_startAt_blank() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", 1000, 0, " ", nowStr(), nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startAt은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: endAt이 null")
    void fail_endAt_null() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", 1000, 0, nowStr(), null, nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endAt은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: endAt이 빈 문자열")
    void fail_endAt_blank() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", 1000, 0, nowStr(), " ", nowStr()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endAt은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: createdAt이 null")
    void fail_createdAt_null() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", 1000, 0, nowStr(), nowStr(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("createdAt은 null이거나 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: createdAt이 빈 문자열")
    void fail_createdAt_blank() {
        assertThatThrownBy(() -> new CouponIssueDto(1L, "FIXED", "desc", 1000, 0, nowStr(), nowStr(), " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("createdAt은 null이거나 비어 있을 수 없습니다.");
    }
}
