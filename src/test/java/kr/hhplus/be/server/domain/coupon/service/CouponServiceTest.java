package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.application.publisher.MessagePublisher;
import kr.hhplus.be.server.domain.coupon.dto.request.*;
import kr.hhplus.be.server.domain.coupon.dto.response.*;
import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponIssuePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    private CouponRepository couponRepository;
    private CouponIssueRepository couponIssueRepository;
    private CouponRedisRepository couponRedisRepository;
    private CouponService couponService;
    private MessagePublisher<CouponIssuePayload> couponIssuePayloadPublisher;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        couponIssueRepository = mock(CouponIssueRepository.class);
        couponRedisRepository = mock(CouponRedisRepository.class);
        couponIssuePayloadPublisher = mock(MessagePublisher.class);

        couponService = new CouponService(
                couponRepository,
                couponIssueRepository,
                couponRedisRepository,
                couponIssuePayloadPublisher
        );
    }

    private String now() {
        return LocalDateTime.now().toString();
    }

    private String nowPlus(int days) {
        return LocalDateTime.now().plusDays(days).toString();
    }

    @Test
    @DisplayName("성공: 활성 쿠폰들을 Redis에 동기화")
    void syncAllActiveCouponsToRedis_success() throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {
        // given
        List<Coupon> activeCoupons = List.of(
                Coupon.builder()
                        .id(1L)
                        .type(CouponType.FIXED)
                        .description("1000원 할인")
                        .discount(1000)
                        .quantity(50)
                        .state(0)
                        .expirationDays(7)
                        .createdAt("2024-05-15T00:00:00")
                        .updatedAt("2024-05-15T00:00:00")
                        .build(),
                Coupon.builder()
                        .id(2L)
                        .type(CouponType.FIXED)
                        .description("2000원 할인")
                        .discount(2000)
                        .quantity(30)
                        .state(0)
                        .expirationDays(14)
                        .createdAt("2024-05-15T00:00:00")
                        .updatedAt("2024-05-15T00:00:00")
                        .build()
        );

        when(couponRepository.findActiveCoupons()).thenReturn(activeCoupons);
        when(couponRedisRepository.existsStock(1L)).thenReturn(false);
        when(couponRedisRepository.existsStock(2L)).thenReturn(true);

        // when
        couponService.syncAllActiveCouponsToRedis();

        // then
        verify(couponRepository).findActiveCoupons();
        verify(couponRedisRepository, times(2)).saveCouponInfo(anyLong(), anyString());
        verify(couponRedisRepository).existsStock(1L);
        verify(couponRedisRepository).existsStock(2L);
        verify(couponRedisRepository).saveStock(1L, 50);
        verify(couponRedisRepository, never()).saveStock(eq(2L), anyInt());
    }

    @Nested
    @DisplayName("applyCouponDiscount 테스트")
    class ApplyCouponDiscountTests {

        @Test
        @DisplayName("발급받은 쿠폰이 없으면 예외 발생")
        void applyCouponDiscount_couponNotIssued() {
            // given
            Long userId = 1L;
            Long couponId = 10L;
            Long couponIssueId = 2L;
            long originalPrice = 10000L;

            ApplyCouponDiscountServiceRequest request = new ApplyCouponDiscountServiceRequest(
                    couponId, couponIssueId, userId, originalPrice
            );

            when(couponRedisRepository.findAllIssuedCoupons(userId)).thenReturn(Map.of());

            // when, then
            assertThatThrownBy(() -> couponService.applyCouponDiscount(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("발급된 쿠폰이 없습니다.");
        }

        @Test
        @DisplayName("이미 사용된 쿠폰이면 예외 발생")
        void applyCouponDiscount_couponAlreadyUsed() {
            // given
            Long userId = 1L;
            Long couponId = 10L;
            Long couponIssueId = 2L;
            long originalPrice = 10000L;

            ApplyCouponDiscountServiceRequest request = new ApplyCouponDiscountServiceRequest(
                    couponId, couponIssueId, userId, originalPrice
            );

            CouponIssueRedisDto usedCouponDto = new CouponIssueRedisDto(couponIssueId, 1);
            when(couponRedisRepository.findAllIssuedCoupons(userId)).thenReturn(
                    Map.of(couponId, usedCouponDto)
            );

            when(couponRedisRepository.findCouponInfo(couponId)).thenReturn(Optional.of(
                    "{\"id\":10,\"type\":\"FIXED\",\"description\":\"테스트 할인\",\"discount\":1000,\"expirationDays\":30}"
            ));

            // when, then
            assertThatThrownBy(() -> couponService.applyCouponDiscount(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 사용된 쿠폰입니다.");
        }

        @Test
        @DisplayName("성공: 할인 적용된 가격 반환")
        void applyCouponDiscount_success() {
            // given
            Long userId = 1L;
            Long couponId = 10L;
            Long couponIssueId = 2L;
            long originalPrice = 10000L;

            ApplyCouponDiscountServiceRequest request = new ApplyCouponDiscountServiceRequest(
                    couponId, couponIssueId, userId, originalPrice
            );

            CouponIssueRedisDto unusedCoupon = new CouponIssueRedisDto(couponIssueId, 0);
            when(couponRedisRepository.findAllIssuedCoupons(userId)).thenReturn(Map.of(couponId, unusedCoupon));
            when(couponRedisRepository.findCouponInfo(couponId)).thenReturn(Optional.of(
                    "{\"id\":10,\"type\":\"FIXED\",\"description\":\"2000원 할인\",\"discount\":2000,\"expirationDays\":30}"
            ));

            // when
            ApplyCouponDiscountServiceResponse response = couponService.applyCouponDiscount(request);

            // then
            assertThat(response.finalPrice()).isEqualTo(8000L);
        }
    }

    @Test
    @DisplayName("예외: findAllIssuedCoupons가 null을 반환할 때 예외 발생")
    void getCoupons_findAllIssuedCouponsReturnsNull() {
        // given
        Long userId = 1L;
        GetCouponsServiceRequest request = new GetCouponsServiceRequest(userId);

        when(couponRedisRepository.findAllIssuedCoupons(userId)).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> couponService.getCoupons(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유한 쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("예외: findCouponInfos가 null을 반환할 때 예외 발생")
    void getCoupons_findCouponInfosReturnsNull() {
        // given
        Long userId = 1L;
        GetCouponsServiceRequest request = new GetCouponsServiceRequest(userId);

        CouponIssueRedisDto unusedCoupon = new CouponIssueRedisDto(null, 0);
        when(couponRedisRepository.findAllIssuedCoupons(userId)).thenReturn(Map.of(1L, unusedCoupon));
        when(couponRedisRepository.findCouponInfos(List.of(1L))).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> couponService.getCoupons(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유한 쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("성공: 쿠폰 발급 성공")
    void issueNewCoupon_success() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        IssueNewCouponServiceRequest request = new IssueNewCouponServiceRequest(userId, couponId);

        when(couponRedisRepository.decreaseStock(couponId)).thenReturn(true);
        when(couponRedisRepository.addCouponForUser(userId, couponId, null, 0)).thenReturn(true);
        when(couponRedisRepository.findCouponInfo(couponId)).thenReturn(
                Optional.of("{\"id\":1,\"type\":\"FIXED\",\"description\":\"테스트 쿠폰\",\"discount\":1000,\"expirationDays\":30}")
        );

        // when
        IssueNewCouponServiceResponse response = couponService.issueNewCoupon(request);

        // then
        assertThat(response.couponId()).isEqualTo(couponId);
    }

    @Test
    @DisplayName("예외: 쿠폰 재고가 없을 때")
    void issueNewCoupon_stockUnavailable() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        IssueNewCouponServiceRequest request = new IssueNewCouponServiceRequest(userId, couponId);

        when(couponRedisRepository.decreaseStock(couponId)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> couponService.issueNewCoupon(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("쿠폰 소진");
    }

    @Test
    @DisplayName("예외: 이미 발급된 유저")
    void issueNewCoupon_alreadyIssuedUser() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        Long couponIssueId = 2L;
        IssueNewCouponServiceRequest request = new IssueNewCouponServiceRequest(userId, couponId);

        when(couponRedisRepository.decreaseStock(couponId)).thenReturn(true);
        when(couponRedisRepository.addCouponForUser(userId, couponId, couponIssueId, 0)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> couponService.issueNewCoupon(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 발급된 유저입니다.");
    }

    @Test
    @DisplayName("예외: 쿠폰 정보가 존재하지 않는 경우")
    void issueNewCoupon_couponInfoNotFound() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        IssueNewCouponServiceRequest request = new IssueNewCouponServiceRequest(userId, couponId);

        when(couponRedisRepository.decreaseStock(couponId)).thenReturn(true);
        when(couponRedisRepository.addCouponForUser(userId, couponId, null, 0)).thenReturn(true);
        when(couponRedisRepository.findCouponInfo(couponId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> couponService.issueNewCoupon(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("쿠폰 정보가 존재하지 않습니다.");
    }
}
