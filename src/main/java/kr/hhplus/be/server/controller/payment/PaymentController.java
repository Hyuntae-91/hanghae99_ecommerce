package kr.hhplus.be.server.controller.payment;

import kr.hhplus.be.server.dto.ErrorResponse;
import kr.hhplus.be.server.dto.payment.PaymentCancelResponse;
import kr.hhplus.be.server.dto.payment.PaymentRequest;
import kr.hhplus.be.server.dto.payment.PaymentResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
public class PaymentController implements PaymentApi {

    @Override
    public ResponseEntity<?> requestPayment(
            @RequestHeader("userId") Long userId,
            @RequestBody PaymentRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        if (request.orderId() == null || request.orderId() == 404L) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Order Not Found"));
        }

        PaymentResponse response = new PaymentResponse(
                1L,
                request.orderId(),
                1,
                10000L,
                "2025-04-03T09:00:00"
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> cancelPayment(
            @RequestHeader("userId") Long userId,
            @PathVariable("paymentId") Long paymentId
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        if (paymentId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Not enough points"));
        }

        if (paymentId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Order Not Found"));
        }

        return ResponseEntity.ok(new PaymentCancelResponse(1L, -1));
    }

}

