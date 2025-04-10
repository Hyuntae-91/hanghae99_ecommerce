package kr.hhplus.be.server.interfaces.api.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.order.dto.AddCartRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/order")
@Tag(name = "Order", description = "주문 관련 API")
public interface OrderApi {

    @Operation(summary = "장바구니 추가", description = "상품을 장바구니에 담는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 추가 성공"),
            @ApiResponse(responseCode = "400", description = "Invalid Request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/cart")
    ResponseEntity<?> addToCart(
            @RequestHeader("userId") Long userId,
            @RequestBody AddCartRequest request
    );
}

