package kr.hhplus.be.server.controller.coupon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.dto.ErrorResponse;
import kr.hhplus.be.server.dto.coupon.CouponIssueResponse;
import kr.hhplus.be.server.dto.coupon.CouponListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/coupon")
@Tag(name = "Coupon", description = "쿠폰 관련 API")
public interface CouponApi {

    @Operation(summary = "보유 쿠폰 조회", description = "사용자의 보유 쿠폰 목록을 응답하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CouponListResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<?> getCoupons(@RequestHeader("userId") Long userId);

    @Operation(summary = "쿠폰 발급", description = "지정한 쿠폰 ID로 사용자에게 쿠폰을 발급하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 성공",
                    content = @Content(schema = @Schema(implementation = CouponIssueResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Coupon Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Coupon Out of Stock",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{couponId}/issue")
    ResponseEntity<?> issueCoupon(
            @RequestHeader("userId") Long userId,
            @PathVariable("couponId") Long couponId
    );

}

