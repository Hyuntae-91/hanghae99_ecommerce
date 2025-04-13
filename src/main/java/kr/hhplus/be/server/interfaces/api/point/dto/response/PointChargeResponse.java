package kr.hhplus.be.server.interfaces.api.point.dto.response;

public record PointChargeResponse(
        Long userId,
        Long totalPoint
) {}