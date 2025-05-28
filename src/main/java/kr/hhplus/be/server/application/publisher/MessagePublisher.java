package kr.hhplus.be.server.application.publisher;

public interface MessagePublisher<T> {
    void publish(String topic, String key, T message);
}
