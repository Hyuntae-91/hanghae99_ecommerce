package kr.hhplus.be.server.application.point.dto;

public record PointChargeResponse(
        Long userId,
        Long totalPoint
) {}