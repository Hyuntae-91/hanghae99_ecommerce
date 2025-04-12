package kr.hhplus.be.server.interfaces.api.point;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.point.dto.request.PointChargeRequest;
import kr.hhplus.be.server.interfaces.api.point.dto.request.PointHistoryRequest;
import kr.hhplus.be.server.interfaces.api.point.dto.response.PointChargeResponse;
import kr.hhplus.be.server.interfaces.api.point.dto.response.PointHistoryResponse;
import kr.hhplus.be.server.interfaces.api.point.dto.response.PointResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/point")
@Tag(name = "Point", description = "포인트 관련 API")
public interface PointApi {

    @Operation(summary = "현재 포인트 조회", description = "유저의 현재 잔액을 응답하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = PointResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<?> getPoint(@RequestHeader("userId") Long userId);

    @Operation(summary = "포인트 내역 조회", description = "유저의 포인트 충전/사용/환불 내역을 확인하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = PointHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    @GetMapping("/history")
    ResponseEntity<?> getHistory(
            @RequestHeader("userId") Long userId,
            @RequestBody PointHistoryRequest request
    );

    @Operation(summary = "포인트 충전", description = "유저의 포인트를 충전하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "충전 성공",
                    content = @Content(schema = @Schema(implementation = PointChargeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid point",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    ResponseEntity<?> chargePoint(
            @RequestHeader("userId") Long userId,
            @RequestBody PointChargeRequest request
    );
}
