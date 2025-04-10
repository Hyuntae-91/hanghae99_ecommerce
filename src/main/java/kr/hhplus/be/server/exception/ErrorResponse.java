package kr.hhplus.be.server.exception;

public record ErrorResponse (
        int code,
        String message
) {}
