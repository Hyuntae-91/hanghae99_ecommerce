package kr.hhplus.be.server.api.controller.coupon;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.ThreadLocalRandom;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CouponConcurrencyTest {

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

        redisContainer.start();
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private CouponIssueJpaRepository couponIssueJpaRepository;

    private String nowPlus(int days) {
        return java.time.LocalDateTime.now().plusDays(days).toString();
    }

    @Test
    @DisplayName("동시성 테스트: 20명의 유저가 동시에 쿠폰 발급 요청을 보냈을 때 실제 발급된 수가 초과될 수 있다")
    void concurrent_coupon_issue_test() throws InterruptedException {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        // 발급 가능한 수량이 3개인 쿠폰 등록
        Coupon coupon = Coupon.builder()
                .type(CouponType.FIXED)
                .description("동시성 테스트 쿠폰")
                .discount(1000)
                .quantity(3)
                .issued(0)
                .expirationDays(7)
                .createdAt(nowPlus(-1))
                .updatedAt(nowPlus(1))
                .build();
        Coupon getCoupon = couponJpaRepository.save(coupon);
        Long couponId = getCoupon.getId();

        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long uid = randomUserId + i;
            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/coupon/" + couponId + "/issue")
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

        long actualIssued = couponIssueJpaRepository.findAll().stream()
                .filter(issue -> issue.getCouponId().equals(couponId))
                .count();

        assertThat(actualIssued).isEqualTo(3);
    }

    @Test
    @DisplayName("동시성 테스트: 20명의 유저가 동시에 쿠폰 발급 요청을 보냈을 때 실제 발급된 수가 초과될 수 있다")
    void concurrent_coupon_issue_test_2() throws InterruptedException {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        // 발급 가능한 수량이 15개인 쿠폰 등록
        Coupon coupon = Coupon.builder()
                .type(CouponType.FIXED)
                .description("동시성 테스트 쿠폰")
                .discount(1000)
                .quantity(15)
                .issued(0)
                .expirationDays(7)
                .createdAt(nowPlus(-1))
                .updatedAt(nowPlus(1))
                .build();
        Coupon getCoupon = couponJpaRepository.save(coupon);
        Long couponId = getCoupon.getId();

        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long uid = randomUserId + i;
            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/coupon/" + couponId + "/issue")
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

        long actualIssued = couponIssueJpaRepository.findAll().stream()
                .filter(issue -> issue.getCouponId().equals(couponId))
                .count();

        assertThat(actualIssued).isEqualTo(15);
    }
}
