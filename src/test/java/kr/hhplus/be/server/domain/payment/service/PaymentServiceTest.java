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
    @DisplayName("성공: 결제 정상 처리 시 상태값 1로 저장되고 응답 반환")
    void pay_success() {
        // given
        Long orderId = 1L;
        Long totalPrice = 5000L;

        // 결제 저장 결과에 ID 부여
        Payment savedPayment = Payment.of(orderId, 1, totalPrice);
        savedPayment.setId(100L); // ID 설정 추가

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentServiceRequest request = new PaymentServiceRequest(
                10L, totalPrice, orderId
        );

        // when
        PaymentServiceResponse result = paymentService.pay(request);

        // then
        assertThat(result.paymentId()).isEqualTo(100L);
        assertThat(result.status()).isEqualTo(1);
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.totalPrice()).isEqualTo(totalPrice);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("실패: 결제 중 예외 발생 시 상태 -1 결제 저장 후 예외 재전파")
    void pay_failure_should_save_failed_payment_and_throw() {
        // given
        Long orderId = 2L;
        Long totalPrice = 10000L;

        // 첫 save는 예외 발생 (정상 시도)
        doThrow(new RuntimeException("DB 장애"))
                .when(paymentRepository).save(argThat(p -> p != null && p.getState() == 1));

        // 실패 결제는 정상 저장되도록 설정 (id 설정 필요)
        Payment failedPayment = Payment.of(orderId, -1, totalPrice);
        failedPayment.setId(99L); // ID 설정

        when(paymentRepository.save(argThat(p -> p != null && p.getState() == -1)))
                .thenReturn(failedPayment);

        PaymentServiceRequest request = new PaymentServiceRequest(10L, totalPrice, orderId);

        // when & then
        assertThatThrownBy(() -> paymentService.pay(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB 장애");

        // verify
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentRepository).save(argThat(p -> p != null && p.getState() == -1));
    }
}
