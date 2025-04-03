package kr.hhplus.be.server.controller.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.dto.ErrorResponse;
import kr.hhplus.be.server.dto.order.OrderCancelResponse;
import kr.hhplus.be.server.dto.order.OrderRequest;
import kr.hhplus.be.server.dto.order.OrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/order")
@Tag(name = "Order", description = "주문 관련 API")
public interface OrderApi {

    @Operation(summary = "주문 생성", description = "상품을 주문하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid Request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found / Coupon Not Found / Order Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Product Out of Stock",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<?> createOrder(
            @RequestHeader("userId") Long userId,
            @RequestBody OrderRequest request
    );

    @Operation(summary = "주문 취소", description = "주문 ID를 기반으로 주문을 취소하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "취소 성공",
                    content = @Content(schema = @Schema(implementation = OrderCancelResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found / Order Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{orderId}/cancel")
    ResponseEntity<?> cancelOrder(
            @RequestHeader("userId") Long userId,
            @PathVariable("orderId") Long orderId
    );
}

