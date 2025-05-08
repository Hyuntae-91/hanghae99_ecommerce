package kr.hhplus.be.server.exception.custom;

public class PointNotEnoughException  extends RuntimeException {
    public PointNotEnoughException(String message) {
        super(message);
    }
}
