package kr.hhplus.be.server.interfaces.api.point.dto;

import kr.hhplus.be.server.domain.point.dto.PointHistoryServiceResponse;

import java.util.List;

public record PointHistoryResponse(
        List<PointHistoryServiceResponse> history
) {}
