package kr.hhplus.be.server.controller.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.dto.ErrorResponse;
import kr.hhplus.be.server.dto.payment.PaymentCancelResponse;
import kr.hhplus.be.server.dto.payment.PaymentRequest;
import kr.hhplus.be.server.dto.payment.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/payment")
@Tag(name = "Payment", description = "결제 관련 API")
public interface PaymentApi {

    @Operation(summary = "결제 요청", description = "주문에 대한 결제를 요청하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 성공",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found / Order Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<?> requestPayment(
            @RequestHeader("userId") Long userId,
            @RequestBody PaymentRequest request
    );

    @Operation(summary = "결제 취소", description = "결제 ID를 기반으로 결제를 취소하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "취소 성공",
                    content = @Content(schema = @Schema(implementation = PaymentCancelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Not enough points",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found / Order Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{paymentId}/cancel")
    ResponseEntity<?> cancelPayment(
            @RequestHeader("userId") Long userId,
            @PathVariable("paymentId") Long paymentId
    );

}
