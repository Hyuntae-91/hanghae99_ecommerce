package kr.hhplus.be.server.domain.payment.mapper;

import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.point.dto.event.PointUsedCompletedEvent;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    default PaymentServiceRequest toPaymentServiceRequest(PointUsedCompletedEvent event) {
        return new PaymentServiceRequest(
                event.userId(),
                event.usedPoint(),
                event.orderId()
        );
    }

    default PaymentCompletedEvent toPaymentCompletedEvent(PaymentServiceResponse response, List<Long> productIds) {
        return new PaymentCompletedEvent(
                response.paymentId(),
                response.orderId(),
                response.status(),
                response.totalPrice(),
                response.createdAt(),
                productIds
        );
    }

}
