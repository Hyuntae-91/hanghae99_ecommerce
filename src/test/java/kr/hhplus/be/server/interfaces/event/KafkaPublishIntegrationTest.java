package kr.hhplus.be.server.interfaces.event;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import kr.hhplus.be.server.infrastructure.order.repository.OrderJpaRepository;
import kr.hhplus.be.server.testhelper.TestKafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Testcontainers
public class KafkaPublishIntegrationTest {

    @TestConfiguration
    static class KafkaTestConsumerConfig {
        @Bean
        public TestKafkaConsumer testKafkaConsumer() {
            return new TestKafkaConsumer();
        }
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        kafkaContainer.start();
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

        registry.add("test.kafka.topic", () -> Topics.MOCK_API_TOPIC);
        registry.add("test.kafka.group", () -> "test-consumer-group");
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TestKafkaConsumer testKafkaConsumer;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Container
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.0")
            .withExposedPorts(6379);

    @Container
    static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"));

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        kafkaContainer.start();
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

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

    @Test
    @Transactional
    void whenPaymentEventPublished_thenMessageSentToKafka() throws Exception {
        // given
        Order order = Order.create(1L, 100_000L);
        Order savedOrder = orderJpaRepository.save(order);
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                1L,
                order.getId(),
                1,
                100_000L,
                "2025-05-28",
                List.of(1L)
        );

        // when
        eventPublisher.publishEvent(event);
        TestTransaction.flagForCommit();  // 트랜잭션 커밋 예정 표시
        TestTransaction.end();            // 트랜잭션 실제 커밋 (AFTER_COMMIT 이벤트 트리거됨)

        // then (간단한 polling 대기)
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> !testKafkaConsumer.getMessages().isEmpty());

        assertThat(testKafkaConsumer.getMessages())
                .anyMatch(msg -> msg.contains("1"));  // orderId: 1L
    }

}
