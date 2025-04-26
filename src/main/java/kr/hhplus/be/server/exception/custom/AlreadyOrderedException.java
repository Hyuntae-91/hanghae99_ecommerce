package kr.hhplus.be.server.exception.custom;

public class AlreadyOrderedException extends RuntimeException {
    public AlreadyOrderedException(String message) { super(message);}
}
