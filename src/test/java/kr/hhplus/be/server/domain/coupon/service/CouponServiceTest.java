package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.dto.request.*;
import kr.hhplus.be.server.domain.coupon.dto.response.CouponIssueDto;
import kr.hhplus.be.server.domain.coupon.dto.response.GetCouponsServiceResponse;
import kr.hhplus.be.server.domain.coupon.dto.response.IssueNewCouponServiceResponse;
import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.exception.custom.InvalidCouponUseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    private CouponRepository couponRepository;
    private CouponIssueRepository couponIssueRepository;
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        couponIssueRepository = mock(CouponIssueRepository.class);
        couponService = new CouponService(couponRepository, couponIssueRepository);
    }

    @Test
    @DisplayName("성공: 단일 쿠폰 이슈 조회")
    void get_coupon_issue_by_id_success() {
        // given
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 5, 7, now(), now());

        CouponIssue issue = CouponIssue.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .state(0)
                .startAt(now())
                .endAt(nowPlus(1))
                .createdAt(now())
                .updatedAt(now())
                .build();

        when(couponIssueRepository.findById(1L)).thenReturn(issue);
        when(couponRepository.findById(1L)).thenReturn(coupon);

        // when
        CouponIssueDto dto = couponService.getCouponIssueById(new GetCouponIssueServiceRequest(1L));

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.couponId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공: 사용자 쿠폰 목록 조회")
    void get_coupons_success() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 5, 3, nowPlus(-2), nowPlus(1));

        CouponIssue issue = CouponIssue.builder()
                .id(1L).userId(1L).couponId(1L).state(0)
                .startAt(nowPlus(-1)).endAt(nowPlus(3))
                .createdAt(now()).updatedAt(now()).build();

        when(couponRepository.findById(1L)).thenReturn(coupon);
        when(couponIssueRepository.findUsableByUserId(1L)).thenReturn(List.of(issue));

        GetCouponsServiceResponse result = couponService.getCoupons(new GetCouponsServiceRequest(1L));

        assertThat(result.coupons()).hasSize(1);
    }

    @Test
    @DisplayName("성공: 쿠폰 발급")
    void issue_new_coupon_success() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 5, 3, now(), now());
        when(couponRepository.findWithLockById(1L)).thenReturn(coupon);

        CouponIssue newIssue = CouponIssue.builder()
                .couponId(1L).userId(1L).state(0)
                .startAt(now()).endAt(nowPlus(3))
                .createdAt(now()).updatedAt(now()).build();

        // 실제 저장될 CouponIssue 객체 생성
        when(couponIssueRepository.save(any())).thenReturn(newIssue);
        when(couponRepository.save(any())).thenReturn(coupon);

        IssueNewCouponServiceResponse result = couponService.issueNewCoupon(new IssueNewCouponServiceRequest(1L, 1L));

        assertThat(result).isNotNull();
        assertThat(result.couponId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패: 쿠폰 발급 수량 초과")
    void issue_new_coupon_fail_quantity_exceeded() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 10, 3, now(), now());
        when(couponRepository.findWithLockById(1L)).thenReturn(coupon);

        assertThatThrownBy(() -> couponService.issueNewCoupon(new IssueNewCouponServiceRequest(1L, 1L)))
                .isInstanceOf(InvalidCouponUseException.class)
                .hasMessageContaining("쿠폰 발급 수량을 초과했습니다");
    }

    @Test
    @DisplayName("성공: 쿠폰 상태 업데이트")
    void save_state_success() {
        CouponIssue issue = mock(CouponIssue.class);
        when(couponIssueRepository.findById(1L)).thenReturn(issue);

        couponService.saveState(new SaveCouponStateRequest(1L, 1));

        verify(issue, times(1)).updateState(1);
        verify(couponIssueRepository).save(issue);
    }

    @Test
    @DisplayName("실패: 쿠폰 상태가 사용불가")
    void apply_coupon_discount_invalid_state() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 5, 3, now(), now());
        CouponIssue issue = CouponIssue.builder()
                .id(1L).userId(1L).couponId(1L).state(1)
                .startAt(now()).endAt(nowPlus(1))
                .createdAt(now()).updatedAt(now()).build();

        when(couponIssueRepository.findById(1L)).thenReturn(issue);
        when(couponRepository.findById(1L)).thenReturn(coupon);

        assertThatThrownBy(() -> couponService.applyCouponDiscount(new ApplyCouponDiscountServiceRequest(1L, 5000L)))
                .isInstanceOf(InvalidCouponUseException.class)
                .hasMessageContaining("사용할 수 없는 쿠폰입니다");
    }

    @Test
    @DisplayName("실패: 쿠폰 유효기간 초과")
    void apply_coupon_discount_expired() {
        String start = LocalDateTime.now().plusDays(1).toString();
        String end = LocalDateTime.now().plusDays(2).toString();
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 5, 3, now(), now());
        CouponIssue issue = CouponIssue.builder()
                .id(1L).userId(1L).couponId(1L).state(0)
                .startAt(start).endAt(end)
                .createdAt(now()).updatedAt(now()).build();

        when(couponIssueRepository.findById(1L)).thenReturn(issue);
        when(couponRepository.findById(1L)).thenReturn(coupon);

        assertThatThrownBy(() -> couponService.applyCouponDiscount(new ApplyCouponDiscountServiceRequest(1L, 5000L)))
                .isInstanceOf(InvalidCouponUseException.class)
                .hasMessageContaining("쿠폰 사용 가능 기간이 아닙니다");
    }

    @Test
    @DisplayName("성공: 쿠폰 없이 할인 적용 요청")
    void apply_coupon_discount_without_coupon_success() {
        ApplyCouponDiscountServiceRequest request = new ApplyCouponDiscountServiceRequest(null, 5000L);
        var result = couponService.applyCouponDiscount(request);

        assertThat(result).isNotNull();
        assertThat(result.finalPrice()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("실패: 할인 요청 originalPrice가 음수일 경우 예외 발생")
    void apply_coupon_discount_invalid_originalPrice() {
        assertThatThrownBy(() -> new ApplyCouponDiscountServiceRequest(null, -100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("originalPrice는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("성공: 쿠폰 정상 적용 시 할인 금액 계산")
    void apply_coupon_discount_success() {
        Coupon coupon = new Coupon(1L, CouponType.FIXED, "desc", 1000, 10, 0, 7, now(), now());
        CouponIssue issue = CouponIssue.builder()
                .id(1L).userId(1L).couponId(1L).state(0)
                .startAt(nowPlus(-1)).endAt(nowPlus(1))
                .createdAt(now()).updatedAt(now()).build();

        when(couponIssueRepository.findById(1L)).thenReturn(issue);
        when(couponRepository.findById(1L)).thenReturn(coupon);

        var result = couponService.applyCouponDiscount(new ApplyCouponDiscountServiceRequest(1L, 5000L));
        assertThat(result.finalPrice()).isEqualTo(4000L);
    }




    private String now() {
        return LocalDateTime.now().toString();
    }

    private String nowPlus(int days) {
        return LocalDateTime.now().plusDays(days).toString();
    }
}
