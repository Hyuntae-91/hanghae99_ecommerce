package kr.hhplus.be.server.testhelper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static kr.hhplus.be.server.common.constants.Topics.MOCK_API_TOPIC;

@Component
public class TestKafkaConsumer {

    private final List<String> messages = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = MOCK_API_TOPIC, groupId = "test-consumer-group")
    public void listen(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}