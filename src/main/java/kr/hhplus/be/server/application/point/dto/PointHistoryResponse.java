package kr.hhplus.be.server.application.point.dto;

import java.util.List;

public record PointHistoryResponse(
        List<PointHistoryDto> history
) {}
