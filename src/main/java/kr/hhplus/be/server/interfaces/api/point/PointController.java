package kr.hhplus.be.server.interfaces.api.point;

import kr.hhplus.be.server.domain.point.dto.*;
import kr.hhplus.be.server.interfaces.api.point.dto.*;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class PointController implements PointApi {

    private final PointService pointService;

    @Override
    public ResponseEntity<?> getPoint(@RequestHeader("userId") Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(404, "User Not Found"));
        }

        UserPointResponseDto result = pointService.getUserPoint(new UserPointRequestDto(userId));
        PointResponse response = PointResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getHistory(@RequestHeader("userId") Long userId, @RequestBody PointHistoryRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "User Not Found"));
        }
        List<PointHistoryResponseDto> result = pointService.getHistory(new PointHistoryRequestDto(
                userId, request.page(), request.size(), request.sort()
        ));

        PointHistoryResponse response = new PointHistoryResponse(result);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> chargePoint(
            @RequestHeader("userId") Long userId,
            @RequestBody PointChargeRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }
        request.validate();
        PointChargeResponseDto result = pointService.charge(new PointChargeRequestDto(userId, request.point()));

        PointChargeResponse response = new PointChargeResponse(userId, result.point());
        return ResponseEntity.ok(response);
    }
}
