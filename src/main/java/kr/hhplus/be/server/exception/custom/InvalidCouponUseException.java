package kr.hhplus.be.server.exception.custom;

public class InvalidCouponUseException extends RuntimeException {
    public InvalidCouponUseException(String message) {
        super(message);
    }
}