package kr.hhplus.be.server.domain.payment.service;

import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentOrderItemDto;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.model.Payment;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private OrderItemRepository orderItemRepository;
    private OrderOptionRepository orderOptionRepository;
    private PaymentRepository paymentRepository;
    private PointRepository userPointRepository;
    private PointHistoryRepository pointHistoryRepository;
    private CouponIssueRepository couponIssueRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository = mock(OrderItemRepository.class);
        orderOptionRepository = mock(OrderOptionRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        userPointRepository = mock(PointRepository.class);
        pointHistoryRepository = mock(PointHistoryRepository.class);
        couponIssueRepository = mock(CouponIssueRepository.class);
        paymentService = new PaymentService(
                orderItemRepository,
                orderOptionRepository,
                paymentRepository,
                userPointRepository,
                pointHistoryRepository,
                couponIssueRepository
        );
    }

    @Test
    @DisplayName("성공: 재고, 포인트 충분한 경우 결제 성공")
    void pay_success() {
        // given
        Long userId = 1L;
        Long orderId = 10L;
        Long orderItemId = 100L;
        Long optionId = 200L;
        long totalPrice = 5000L;

        OrderItem item = mock(OrderItem.class);
        when(item.getId()).thenReturn(orderItemId);
        when(item.getOptionId()).thenReturn(optionId);
        when(item.getQuantity()).thenReturn(2);

        OrderOption option = mock(OrderOption.class);
        when(option.getStockQuantity()).thenReturn(10);

        UserPoint userPoint = mock(UserPoint.class);
        when(userPoint.getPoint()).thenReturn(10000L);

        Payment payment = Payment.of(orderId, 1, totalPrice);

        when(orderItemRepository.findByIds(List.of(orderItemId))).thenReturn(List.of(item));
        when(orderOptionRepository.getById(optionId)).thenReturn(option);
        when(userPointRepository.get(userId)).thenReturn(userPoint);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                totalPrice,
                1L,
                List.of(new PaymentOrderItemDto(orderId, orderItemId, optionId))
        );

        // when
        PaymentServiceResponse result = paymentService.pay(request);

        // then
        assertThat(result.status()).isEqualTo(1);
        assertThat(result.orderId()).isEqualTo(orderId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("실패: 재고 부족 시 결제 실패 처리")
    void pay_fail_due_to_stock() {
        // given
        Long userId = 1L;
        Long orderItemId = 1L;
        Long optionId = 10L;
        Long orderId = 99L;

        OrderItem item = mock(OrderItem.class);
        when(item.getId()).thenReturn(orderItemId);
        when(item.getOptionId()).thenReturn(optionId);
        when(item.getQuantity()).thenReturn(5);

        OrderOption option = mock(OrderOption.class);
        when(option.getStockQuantity()).thenReturn(2);  // 재고 부족

        Payment failedPayment = Payment.of(orderId, -1, 1000L);

        when(orderItemRepository.findByIds(List.of(orderItemId))).thenReturn(List.of(item));
        when(orderOptionRepository.getById(optionId)).thenReturn(option);
        when(paymentRepository.save(any())).thenReturn(failedPayment);

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                1000L,
                1L,
                List.of(new PaymentOrderItemDto(orderId, orderItemId, optionId))
        );

        // when
        PaymentServiceResponse response = paymentService.pay(request);

        // then
        assertThat(response.status()).isEqualTo(-1);
        verify(orderOptionRepository, never()).save(any());
        verify(userPointRepository, never()).savePoint(any());
    }

    @Test
    @DisplayName("포인트 부족 시 재고 차감, 포인트 차감, 저장은 호출되지 않는다")
    void pay_fail_due_to_point_no_side_effects() {
        // given
        Long userId = 1L;
        Long orderItemId = 1L;
        Long optionId = 2L;
        Long orderId = 123L;

        OrderItem item = mock(OrderItem.class);
        when(item.getId()).thenReturn(orderItemId);
        when(item.getOptionId()).thenReturn(optionId);
        when(item.getQuantity()).thenReturn(1);

        OrderOption option = mock(OrderOption.class);
        when(option.getStockQuantity()).thenReturn(10);

        UserPoint userPoint = mock(UserPoint.class);
        doThrow(new IllegalStateException("포인트가 부족합니다."))
                .when(userPoint).validateUsableBalance(anyLong());

        when(orderItemRepository.findByIds(List.of(orderItemId))).thenReturn(List.of(item));
        when(orderOptionRepository.getById(optionId)).thenReturn(option);
        when(userPointRepository.get(userId)).thenReturn(userPoint);
        when(paymentRepository.save(any())).thenReturn(Payment.of(orderId, -1, 1000L));

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                1000L,
                1L,
                List.of(new PaymentOrderItemDto(orderId, orderItemId, optionId))
        );


        // when
        PaymentServiceResponse response = paymentService.pay(request);

        // then
        assertThat(response.status()).isEqualTo(-1);
        verify(orderOptionRepository, never()).save(any());
        verify(userPoint, never()).use(anyLong());
        verify(userPointRepository, never()).savePoint(any());
    }

    @Test
    @DisplayName("성공: 결제 실패 시 status -1로 저장됨")
    void pay_fail_and_saves_failed_payment() {
        // given
        Long orderId = 10L;
        Long userId = 1L;
        long totalPrice = 9999L;

        when(orderItemRepository.findByIds(any())).thenThrow(new RuntimeException("조회 실패"));

        Payment failed = Payment.of(orderId, -1, totalPrice);
        when(paymentRepository.save(any())).thenReturn(failed);

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                totalPrice,
                1L,
                List.of(new PaymentOrderItemDto(orderId, 999L, 888L))
        );

        // when
        PaymentServiceResponse response = paymentService.pay(request);

        // then
        assertThat(response.status()).isEqualTo(-1);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("성공: 쿠폰 사용 처리")
    void pay_success_with_coupon_usage() {
        // given
        Long userId = 1L;
        Long orderId = 10L;
        Long orderItemId = 100L;
        Long optionId = 200L;
        Long couponIssueId = 300L;
        long totalPrice = 5000L;

        OrderItem item = mock(OrderItem.class);
        when(item.getId()).thenReturn(orderItemId);
        when(item.getOptionId()).thenReturn(optionId);
        when(item.getQuantity()).thenReturn(1);

        OrderOption option = mock(OrderOption.class);
        when(option.getStockQuantity()).thenReturn(10);

        UserPoint point = mock(UserPoint.class);
        when(point.getPoint()).thenReturn(10000L);

        CouponIssue couponIssue = mock(CouponIssue.class);

        when(orderItemRepository.findByIds(List.of(orderItemId))).thenReturn(List.of(item));
        when(orderOptionRepository.getById(optionId)).thenReturn(option);
        when(userPointRepository.get(userId)).thenReturn(point);
        when(couponIssueRepository.findById(couponIssueId)).thenReturn(couponIssue);
        when(paymentRepository.save(any())).thenReturn(Payment.of(orderId, 1, totalPrice));

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                totalPrice,
                couponIssueId,
                List.of(new PaymentOrderItemDto(orderId, orderItemId, optionId))
        );

        // when
        PaymentServiceResponse response = paymentService.pay(request);

        // then
        verify(couponIssue, times(1)).markUsed();
        verify(couponIssueRepository, times(1)).save(couponIssue);
        assertThat(response.status()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공: 쿠폰 없이 결제 처리")
    void pay_success_without_coupon() {
        // given
        Long userId = 1L;
        Long orderId = 10L;
        Long orderItemId = 100L;
        Long optionId = 200L;
        long totalPrice = 5000L;

        OrderItem item = mock(OrderItem.class);
        when(item.getId()).thenReturn(orderItemId);
        when(item.getOptionId()).thenReturn(optionId);
        when(item.getQuantity()).thenReturn(1);

        OrderOption option = mock(OrderOption.class);
        when(option.getStockQuantity()).thenReturn(10);

        UserPoint userPoint = mock(UserPoint.class);
        when(userPoint.getPoint()).thenReturn(10000L);

        when(orderItemRepository.findByIds(any())).thenReturn(List.of(item));
        when(orderOptionRepository.getById(optionId)).thenReturn(option);
        when(userPointRepository.get(any())).thenReturn(userPoint);
        when(paymentRepository.save(any())).thenReturn(Payment.of(orderId, 1, totalPrice));

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                totalPrice,
                null,
                List.of(new PaymentOrderItemDto(orderId, orderItemId, optionId))
        );

        // when
        PaymentServiceResponse response = paymentService.pay(request);

        // then
        assertThat(response.status()).isEqualTo(1);
        verify(couponIssueRepository, never()).findById(any());
        verify(couponIssueRepository, never()).save(any());
    }


    @Test
    @DisplayName("실패: 쿠폰 상태가 사용 불가 상태인 경우")
    void pay_fail_coupon_invalid_state() {
        // given
        Long userId = 1L;
        Long orderId = 10L;
        Long orderItemId = 100L;
        Long optionId = 200L;
        Long couponIssueId = 300L;
        long totalPrice = 5000L;

        OrderItem item = mock(OrderItem.class);
        when(item.getId()).thenReturn(orderItemId);
        when(item.getOptionId()).thenReturn(optionId);
        when(item.getQuantity()).thenReturn(1);

        OrderOption option = mock(OrderOption.class);
        when(option.getStockQuantity()).thenReturn(10);

        UserPoint userPoint = mock(UserPoint.class);
        when(userPoint.getPoint()).thenReturn(10000L);

        CouponIssue issue = mock(CouponIssue.class);
        doThrow(new IllegalStateException("사용할 수 없는 쿠폰입니다.")).when(issue).markUsed();

        when(orderItemRepository.findByIds(any())).thenReturn(List.of(item));
        when(orderOptionRepository.getById(optionId)).thenReturn(option);
        when(userPointRepository.get(any())).thenReturn(userPoint);
        when(couponIssueRepository.findById(couponIssueId)).thenReturn(issue);
        when(paymentRepository.save(any())).thenReturn(Payment.of(orderId, -1, totalPrice));

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                totalPrice,
                couponIssueId,
                List.of(new PaymentOrderItemDto(orderId, orderItemId, optionId))
        );

        // when
        PaymentServiceResponse response = paymentService.pay(request);

        // then
        assertThat(response.status()).isEqualTo(-1);
        verify(issue).markUsed();
    }

}
