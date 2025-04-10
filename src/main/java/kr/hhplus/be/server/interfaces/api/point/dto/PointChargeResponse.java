package kr.hhplus.be.server.interfaces.api.point.dto;

public record PointChargeResponse(
        Long userId,
        Long totalPoint
) {}