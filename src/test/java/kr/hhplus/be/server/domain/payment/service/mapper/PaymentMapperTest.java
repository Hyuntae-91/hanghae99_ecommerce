package kr.hhplus.be.server.domain.payment.service.mapper;

import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.mapper.PaymentMapper;
import kr.hhplus.be.server.domain.payment.mapper.PaymentMapperImpl;
import kr.hhplus.be.server.domain.point.dto.event.PointUsedCompletedEvent;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentCompletedPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUsedCompletedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductDataIds;
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
    @DisplayName("성공: PointUsedCompletedPayload → PaymentServiceRequest 매핑")
    void map_toPaymentServiceRequest_success() {
        List<ProductDataIds> items = List.of(
                new ProductDataIds(1L, 101L, 1001L, 1),
                new ProductDataIds(2L, 102L, 1002L, 1)
        );
        PointUsedCompletedPayload event = new PointUsedCompletedPayload(
                1L, 10L, 5000L, 1L, 1L, items
        );

        PaymentServiceRequest result = mapper.toPaymentServiceRequest(event);

        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.totalPrice()).isEqualTo(5000L);
        assertThat(result.orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공: PaymentServiceResponse + productIds → PaymentCompletedEvent 매핑")
    void map_toPaymentCompletedPayload_success() {
        PaymentServiceResponse response = new PaymentServiceResponse(
                123L, 456L, 1, 10000L, "2024-01-01T12:00:00"
        );

    }
}
