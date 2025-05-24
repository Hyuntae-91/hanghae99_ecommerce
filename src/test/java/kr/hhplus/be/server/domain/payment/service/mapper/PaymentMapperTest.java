package kr.hhplus.be.server.domain.payment.service.mapper;

import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.mapper.PaymentMapper;
import kr.hhplus.be.server.domain.payment.mapper.PaymentMapperImpl;
import kr.hhplus.be.server.domain.point.dto.event.PointUsedCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    private PaymentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentMapperImpl(); // MapStruct 생성된 구현체 사용
    }

    @Test
    @DisplayName("성공: PointUsedCompletedEvent → PaymentServiceRequest 매핑")
    void map_toPaymentServiceRequest_success() {
        PointUsedCompletedEvent event = new PointUsedCompletedEvent(
                1L, 10L, 5000L, List.of(100L, 200L)
        );

        PaymentServiceRequest result = mapper.toPaymentServiceRequest(event);

        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.totalPrice()).isEqualTo(5000L);
        assertThat(result.orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공: PaymentServiceResponse + productIds → PaymentCompletedEvent 매핑")
    void map_toPaymentCompletedEvent_success() {
        PaymentServiceResponse response = new PaymentServiceResponse(
                123L, 456L, 1, 10000L, "2024-01-01T12:00:00"
        );

        List<Long> productIds = List.of(1L, 2L, 3L);

        PaymentCompletedEvent result = mapper.toPaymentCompletedEvent(response, productIds);

        assertThat(result.paymentId()).isEqualTo(123L);
        assertThat(result.orderId()).isEqualTo(456L);
        assertThat(result.status()).isEqualTo(1);
        assertThat(result.totalPrice()).isEqualTo(10000L);
        assertThat(result.createdAt()).isEqualTo("2024-01-01T12:00:00");
        assertThat(result.productIds()).containsExactly(1L, 2L, 3L);
    }
}
