package kr.hhplus.be.server.application.point;

public record PointChargeResponse(
        Long userId,
        Long totalPoint
) {}