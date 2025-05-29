package kr.hhplus.be.server.testhelper;

import kr.hhplus.be.server.common.constants.Topics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TestKafkaConsumer {

    private final List<String> messages = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "${test.kafka.topic}", groupId = "${test.kafka.group}")
    public void listen(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}