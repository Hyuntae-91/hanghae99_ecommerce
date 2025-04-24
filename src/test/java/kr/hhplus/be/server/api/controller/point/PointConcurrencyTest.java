package kr.hhplus.be.server.api.controller.point;

import kr.hhplus.be.server.domain.point.dto.request.PointChargeServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.testhelper.RepositoryCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PointConcurrencyTest {

    @Container
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        if (!mysqlContainer.isRunning()) {
            mysqlContainer.start();
        }
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQL8Dialect");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");
    }

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    @BeforeEach
    void cleanup() {
        repositoryCleaner.cleanUpAll();
    }

    @Test
    @DisplayName("동시성 테스트: 여러 스레드가 동일한 유저에게 포인트를 동시에 충전")
    void concurrentChargeUserPoint_WithCountDownLatch() throws InterruptedException {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        int threadCount = 10;
        long chargeAmount = 1000;

        pointRepository.savePoint(UserPoint.of(randomUserId, 0L));
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    pointService.charge(new PointChargeServiceRequest(randomUserId, chargeAmount));
                } catch (Exception e) {
                    System.err.println("충전 중 예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();

        // 결과 검증
        UserPointServiceResponse result = pointService.getUserPoint(new UserPointServiceRequest(randomUserId));
        assertEquals(threadCount * chargeAmount, result.point());
    }

    @Test
    @DisplayName("동시성 테스트: 여러 유저가 각각 동시에 포인트 동시에 충전")
    void concurrentChargeUserPointMultipleUsers() throws InterruptedException {
        int USER_COUNT = 3;
        int THREAD_COUNT_PER_USER = 10;
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long chargeAmount = 1000L;

        CountDownLatch latch = new CountDownLatch(USER_COUNT * THREAD_COUNT_PER_USER);
        for (int i = 0; i < USER_COUNT; i++) {
            long userId = randomUserId + i;
            pointRepository.savePoint(UserPoint.of(userId, 0L));
            for (int j = 0; j < THREAD_COUNT_PER_USER; j++) {
                new Thread(() -> {
                    try {
                        pointService.charge(new PointChargeServiceRequest(userId, chargeAmount));
                    } catch (Exception e) {
                        System.err.println("유저 " + userId + " 충전 중 예외 발생: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }
        }
        latch.await();

        for (long userId = randomUserId; userId <= USER_COUNT; userId++) {
            UserPointServiceResponse result = pointService.getUserPoint(new UserPointServiceRequest(userId));
            assertEquals(THREAD_COUNT_PER_USER * chargeAmount, result.point(), "유저 " + userId + " 포인트 불일치");
        }
    }
}
