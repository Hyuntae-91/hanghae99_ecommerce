package kr.hhplus.be.server.application.point;

import java.util.List;

public record PointHistoryResponse(
        List<PointHistoryDto> history
) {}
