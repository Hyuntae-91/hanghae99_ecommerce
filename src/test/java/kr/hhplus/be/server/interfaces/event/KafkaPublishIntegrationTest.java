package kr.hhplus.be.server.interfaces.event;

import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.interfaces.event.order.OrderUpdateProcessor;
import kr.hhplus.be.server.interfaces.event.product.ProductScoreUpdater;
import kr.hhplus.be.server.interfaces.event.payment.payload.PaymentCompletedPayload;
import kr.hhplus.be.server.testhelper.TestKafkaConsumer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Testcontainers
class KafkaPublishIntegrationTest {

    @TestConfiguration
    static class TestKafkaConsumerConfig {
        @Bean
        TestKafkaConsumer testKafkaConsumer() {
            return new TestKafkaConsumer();
        }
    }

    @Container
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.0")
            .withExposedPorts(6379);

    @MockBean
    OrderUpdateProcessor orderUpdateConsumer;

    @MockBean
    ProductScoreUpdater productScoreConsumer;

    @Container
    static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"));

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("test.kafka.topic", () -> Topics.MOCK_API_TOPIC);
        registry.add("test.kafka.group", () -> "test-consumer-group");
        kafkaContainer.start();
        if (!mysqlContainer.isRunning()) {
            mysqlContainer.start();
        }
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");

        redisContainer.start();
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestKafkaConsumer testKafkaConsumer;

    @Test
    void whenPaymentCompletedMessagePublished_thenMockApiTopicReceivesPayload() {
        // given
        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                1L,
                1L,
                1,
                100_000L,
                "2025-06-02",
                List.of(1L)
        );

        // when
        kafkaTemplate.send(Topics.PAYMENT_COMPLETE_TOPIC, String.valueOf(payload.orderId()), payload);

        // then
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(testKafkaConsumer.getMessages())
                        .anyMatch(msg -> msg.contains("\"orderId\":1")));
    }
}
