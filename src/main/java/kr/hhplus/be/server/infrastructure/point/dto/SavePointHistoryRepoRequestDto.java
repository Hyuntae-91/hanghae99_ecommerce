package kr.hhplus.be.server.infrastructure.point.dto;

import kr.hhplus.be.server.domain.point.model.PointHistoryType;

public record SavePointHistoryRepoRequestDto (
        Long userId,
        Long point,
        PointHistoryType type
){}
