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

        UserPointServiceResponse result = pointService.getUserPoint(new UserPointServiceRequest(userId));
        return ResponseEntity.ok(PointResponse.from(result));
    }

    @Override
    public ResponseEntity<?> getHistory(@RequestHeader("userId") Long userId, @RequestBody PointHistoryRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "User Not Found"));
        }
        List<PointHistoryServiceResponse> result = pointService.getHistory(new PointHistoryServiceRequest(
                userId, request.page(), request.size(), request.sort()
        ));

        return ResponseEntity.ok(PointHistoryResponse.from(result));
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
        PointChargeServiceResponse result = pointService.charge(new PointChargeServiceRequest(userId, request.point()));

        return ResponseEntity.ok(new PointChargeResponse(userId, result.point()));
    }
}
