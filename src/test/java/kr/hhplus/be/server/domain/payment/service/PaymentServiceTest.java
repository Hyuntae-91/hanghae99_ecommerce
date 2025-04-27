package kr.hhplus.be.server.domain.payment.service;

import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentService = new PaymentService(
                paymentRepository
        );
    }

    @Test
    @DisplayName("성공: 재고, 포인트 충분한 경우 결제 성공")
    void pay_success() {
        // given
        Long userId = 1L;
        Long orderId = 10L;
        long totalPrice = 5000L;

        Payment expectedPayment = Payment.of(orderId, 1, totalPrice);
        when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                totalPrice,
                orderId
        );

        // when
        PaymentServiceResponse result = paymentService.pay(request);

        // then
        assertThat(result.status()).isEqualTo(1);
        assertThat(result.orderId()).isEqualTo(orderId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("성공: 결제 실패 시 status -1로 저장됨")
    void pay_fail_and_saves_failed_payment() {
        // given
        Long userId = 1L;
        Long orderId = 10L;
        long totalPrice = 9999L;

        Payment expectedFailedPayment = Payment.builder()
                .id(1L)
                .orderId(orderId)
                .state(-1)
                .totalPrice(totalPrice)
                .createdAt(java.time.LocalDateTime.now().toString())
                .updatedAt(java.time.LocalDateTime.now().toString())
                .build();

        when(paymentRepository.save(any(Payment.class)))
                .thenThrow(new RuntimeException("DB 에러"))
                .thenReturn(expectedFailedPayment);

        PaymentServiceRequest request = new PaymentServiceRequest(
                userId,
                totalPrice,
                orderId
        );

        // when
        PaymentServiceResponse result = paymentService.pay(request);

        // then
        assertThat(result.status()).isEqualTo(-1);
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

}
