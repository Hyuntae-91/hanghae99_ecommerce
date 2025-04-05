package kr.hhplus.be.server.dto;

public record ErrorResponse (
        int code,
        String message
) {}
