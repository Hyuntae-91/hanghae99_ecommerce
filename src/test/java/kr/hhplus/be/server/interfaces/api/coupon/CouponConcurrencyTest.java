package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.domain.coupon.dto.response.CouponIssueRedisDto;
import kr.hhplus.be.server.domain.coupon.mapper.CouponJsonMapper;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CouponConcurrencyTest {

    @Container
    static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"));

    @Container
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.0")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("test.kafka.topic", () -> "test");
        registry.add("test.kafka.group", () -> "test");
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
    private MockMvc mockMvc;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private CouponRedisRepository couponRedisRepository;

    @Autowired
    private CouponIssueJpaRepository couponIssueJpaRepository;

    private String nowPlus(int days) {
        return java.time.LocalDateTime.now().plusDays(days).toString();
    }

    @Test
    @DisplayName("동시성 테스트: 20명 요청, 쿠폰 수량 3개")
    void concurrent_coupon_issue_test_limit_3() throws InterruptedException {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long randomCouponId = ThreadLocalRandom.current().nextInt(1, 100_000);

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .id(randomCouponId)
                .type(CouponType.FIXED)
                .description("동시성 테스트 쿠폰 3개")
                .discount(1000)
                .quantity(3)
                .state(1)
                .expirationDays(7)
                .createdAt(nowPlus(-1))
                .updatedAt(nowPlus(1))
                .build());

        // Redis 초기화
        couponRedisRepository.saveCouponInfo(randomCouponId, CouponJsonMapper.toCouponJson(coupon));
        couponRedisRepository.saveStock(randomCouponId, coupon.getQuantity());

        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long uid = randomUserId + i;
            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/coupon/{couponId}/issue", randomCouponId)
                                    .header("userId", uid)
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andReturn();
                } catch (Exception e) {
                    System.out.println("에러 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        long issuedCount = 0;
        for (int i = 0; i < threadCount; i++) {
            long userId = randomUserId + i;
            Map<Long, CouponIssueRedisDto> userCoupons = couponRedisRepository.findAllIssuedCoupons(userId);
            CouponIssueRedisDto dto = userCoupons.get(randomCouponId);
            if (dto != null && dto.getUse() == 0) { // 0이면 발급 성공
                issuedCount++;
            }
        }

        Awaitility.await()
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<CouponIssue> result = couponIssueJpaRepository.findAllByCouponId(randomCouponId);
                    assertThat(result.size()).isEqualTo(3);
                });
        assertThat(issuedCount).isEqualTo(3);
    }

    @Test
    @DisplayName("동시성 테스트: 20명 요청, 쿠폰 수량 15개")
    void concurrent_coupon_issue_test_limit_15() throws InterruptedException {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long randomCouponId = ThreadLocalRandom.current().nextInt(1, 100_000);

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .id(randomCouponId)
                .type(CouponType.FIXED)
                .description("동시성 테스트 쿠폰 15개")
                .discount(1000)
                .quantity(15)
                .state(1)
                .expirationDays(7)
                .createdAt(nowPlus(-1))
                .updatedAt(nowPlus(1))
                .build());

        // Redis 초기화
        couponRedisRepository.saveCouponInfo(randomCouponId, CouponJsonMapper.toCouponJson(coupon));
        couponRedisRepository.saveStock(randomCouponId, coupon.getQuantity());

        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long uid = randomUserId + i;
            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/coupon/{couponId}/issue", randomCouponId)
                                    .header("userId", uid)
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andReturn();
                } catch (Exception e) {
                    System.out.println("에러 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        // 모든 유저에 대해 Redis 발급 여부 체크
        long issuedCount = 0;
        for (int i = 0; i < threadCount; i++) {
            long uid = randomUserId + i;
            Map<Long, CouponIssueRedisDto> issued = couponRedisRepository.findAllIssuedCoupons(uid);
            CouponIssueRedisDto dto = issued.get(randomCouponId);
            if (dto != null && dto.getUse() == 0) {
                issuedCount++;
            }
        }

        Awaitility.await()
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<CouponIssue> result = couponIssueJpaRepository.findAllByCouponId(randomCouponId);
                    assertThat(result.size()).isEqualTo(15);
                });
        assertThat(issuedCount).isEqualTo(15);
    }

}
