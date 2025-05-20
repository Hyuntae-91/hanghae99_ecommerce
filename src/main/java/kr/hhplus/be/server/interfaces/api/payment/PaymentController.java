package kr.hhplus.be.server.interfaces.api.payment;

import kr.hhplus.be.server.application.payment.PaymentFacade;
import kr.hhplus.be.server.application.payment.dto.PaymentFacadeMapper;
import kr.hhplus.be.server.application.payment.dto.PaymentFacadeRequest;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
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

    private final PaymentFacade paymentFacade;
    private final PaymentFacadeMapper paymentFacadeMapper;

    @Override
    public ResponseEntity<?> requestPayment(
            @RequestHeader("userId") Long userId,
            @RequestBody PaymentRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        PaymentFacadeRequest facadeRequest = paymentFacadeMapper.toFacadeRequest(request);
        facadeRequest = new PaymentFacadeRequest(userId, facadeRequest.products(), facadeRequest.couponId());
        PaymentServiceResponse result = paymentFacade.pay(facadeRequest);
        return ResponseEntity.ok(PaymentResponse.from(result));
    }
}

