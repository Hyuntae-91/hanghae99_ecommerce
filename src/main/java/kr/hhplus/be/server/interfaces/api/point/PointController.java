package kr.hhplus.be.server.interfaces.api.point;

import kr.hhplus.be.server.application.point.dto.PointChargeRequest;
import kr.hhplus.be.server.application.point.dto.PointChargeResponse;
import kr.hhplus.be.server.application.point.dto.PointHistoryResponse;
import kr.hhplus.be.server.application.point.dto.PointResponse;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;


@RestController
@RequiredArgsConstructor
public class PointController implements PointApi {

    private final PointService pointService;

    @Override
    public ResponseEntity<?> getPoint(Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(404, "User Not Found"));
        }

        PointResponse response = new PointResponse(userId, pointService.getPoint(userId));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getHistory(Long userId, int page, int size, String sort) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "User Not Found"));
        }

        PointHistoryResponse response = new PointHistoryResponse(pointService.getHistory(userId, page, size, sort));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> chargePoint(@RequestHeader("userId") Long userId, @RequestBody PointChargeRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        request.validate();
        PointChargeResponse response = new PointChargeResponse(userId, pointService.charge(userId, request.point()));
        return ResponseEntity.ok(response);
    }
}
