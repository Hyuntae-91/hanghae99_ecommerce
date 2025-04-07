package kr.hhplus.be.server.interfaces.api.order;

import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.application.order.OrderCancelResponse;
import kr.hhplus.be.server.application.order.OrderRequest;
import kr.hhplus.be.server.application.order.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements OrderApi {

    @Override
    public ResponseEntity<?> createOrder(Long userId, OrderRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        if (request.productId() == null || request.quantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Invalid Request"));
        }

        if (request.productId() == 999L) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Product Out of Stock"));
        }

        OrderResponse response = new OrderResponse(
                1L,
                0,
                10000L,
                request.quantity(),
                request.couponIssueId() == null ? 0L : request.couponIssueId(),
                "2025-04-03T09:00:00"
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> cancelOrder(
            @RequestHeader("userId") Long userId,
            @PathVariable("orderId") Long orderId
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        if (orderId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Order Not Found"));
        }

        return ResponseEntity.ok(new OrderCancelResponse(orderId, -1));
    }

}
