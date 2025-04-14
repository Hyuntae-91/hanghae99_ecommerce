package kr.hhplus.be.server.interfaces.api.point;

import jakarta.validation.Valid;
import kr.hhplus.be.server.domain.point.service.PointHistoryService;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.point.dto.request.PointChargeServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.PointHistoryServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.PointChargeServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.PointHistoryServiceResponse;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.point.dto.request.PointChargeRequest;
import kr.hhplus.be.server.interfaces.api.point.dto.request.PointHistoryRequest;
import kr.hhplus.be.server.interfaces.api.point.dto.response.PointChargeResponse;
import kr.hhplus.be.server.interfaces.api.point.dto.response.PointHistoryResponse;
import kr.hhplus.be.server.interfaces.api.point.dto.response.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class PointController implements PointApi {

    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    @Override
    public ResponseEntity<?> getPoint(@RequestHeader("userId") Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(404, "User Not Found"));
        }
        return ResponseEntity.ok(PointResponse.from(pointService.getUserPoint(new UserPointServiceRequest(userId))));
    }

    @Override
    public ResponseEntity<?> getHistory(
            @RequestHeader("userId") Long userId,
            @RequestBody @Valid PointHistoryRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "User Not Found"));
        }
        List<PointHistoryServiceResponse> result = pointHistoryService.getHistory(new PointHistoryServiceRequest(
                userId, request.page(), request.size(), request.sort()
        ));

        return ResponseEntity.ok(PointHistoryResponse.from(result));
    }

    @Override
    public ResponseEntity<?> chargePoint(
            @RequestHeader("userId") Long userId,
            @RequestBody @Valid PointChargeRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }
        PointChargeServiceResponse result = pointService.charge(new PointChargeServiceRequest(userId, request.point()));

        return ResponseEntity.ok(new PointChargeResponse(userId, result.point()));
    }
}
