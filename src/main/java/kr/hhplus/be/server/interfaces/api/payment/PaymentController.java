package kr.hhplus.be.server.interfaces.api.payment;

import kr.hhplus.be.server.domain.order.dto.response.CreateOrderServiceResponse;
import kr.hhplus.be.server.domain.order.mapper.OrderMapper;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.payment.dto.request.PaymentRequest;
import kr.hhplus.be.server.interfaces.api.payment.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Override
    public ResponseEntity<?> requestPayment(
            @RequestHeader("userId") Long userId,
            @RequestBody PaymentRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        CreateOrderServiceResponse orderIdDto = orderService.createOrder(orderMapper.toServiceRequest(userId, request));
        return ResponseEntity.ok(PaymentResponse.from(orderIdDto));
    }
}
