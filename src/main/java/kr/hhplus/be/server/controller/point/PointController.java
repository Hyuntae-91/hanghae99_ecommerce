package kr.hhplus.be.server.controller.point;

import kr.hhplus.be.server.dto.ErrorResponse;
import kr.hhplus.be.server.dto.point.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;


@RestController
public class PointController implements PointApi {
    @Override
    public ResponseEntity<?> getPoint(Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(404, "User Not Found"));
        }

        PointResponse response = new PointResponse(userId, 100L);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getHistory(Long userId, int page, int size, String sort) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "User Not Found"));
        }

        List<PointHistory> history = List.of(
                new PointHistory(userId, 100L, "충전", "2025-04-03T10:20:00"),
                new PointHistory(userId, 200L, "충전", "2025-04-03T10:10:00"),
                new PointHistory(userId, 100L, "결제", "2025-04-03T10:00:00")
        );
        return ResponseEntity.ok(new PointHistoryResponse(history));
    }

    @Override
    public ResponseEntity<?> chargePoint(@RequestHeader("userId") Long userId, @RequestBody PointChargeRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        if (request.point() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Invalid point"));
        }

        // mock total_point 계산 (예: 기존 400 + 충전 100 = 500)
        Long total = 400 + request.point();

        return ResponseEntity.ok(new PointChargeResponse(userId, total));
    }
}
