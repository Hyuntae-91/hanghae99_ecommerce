package kr.hhplus.be.server.api.controller.coupon;

import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponJpaRepository;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CouponControllerTest {

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

    @Test
    @DisplayName("성공: 쿠폰 발급 후 조회까지 전체 흐름 확인")
    void issue_and_get_coupon_success() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        // given
        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("테스트 쿠폰")
                .discount(1000)
                .quantity(10)
                .issued(0)
                .expirationDays(7)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        Long couponId = coupon.getId();

        // when: 쿠폰 발급 API 요청
        mockMvc.perform(post("/v1/coupon/{couponId}/issue", couponId)
                        .header("userId", randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponId").value(couponId))
                .andExpect(jsonPath("$.type").value("FIXED"))
                .andExpect(jsonPath("$.discount").value(1000));

        // then: 실제 DB에 쿠폰 이슈 존재하는지 검증
        var allIssues = couponIssueJpaRepository.findAllByUserId(randomUserId);
        assertThat(allIssues).hasSize(1);
        assertThat(allIssues.get(0).getCouponId()).isEqualTo(couponId);

        // issued 증가했는지 확인
        Coupon updatedCoupon = couponJpaRepository.findById(couponId).orElseThrow();
        assertThat(updatedCoupon.getIssued()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공: 쿠폰 목록 조회")
    void get_coupons_success() throws Exception {
        // given
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("조회용 쿠폰")
                .discount(1000)
                .quantity(10)
                .issued(1)
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build());

        couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(randomUserId)
                .couponId(coupon.getId())
                .state(0)
                .startAt(nowPlus(-1))
                .endAt(nowPlus(7))
                .createdAt(now())
                .updatedAt(now())
                .build());

        // when & then
        mockMvc.perform(get("/v1/coupon")
                        .header("userId", randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coupons").isArray())
                .andExpect(jsonPath("$.coupons.length()").value(1))
                .andExpect(jsonPath("$.coupons[0].couponId").value(coupon.getId()))
                .andExpect(jsonPath("$.coupons[0].discount").value(1000));
    }

    @Test
    @DisplayName("실패: 쿠폰 수량 초과로 발급 실패")
    void issue_coupon_fail_when_quantity_exceeded() throws Exception {
        // given
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        Long couponId = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("초과 쿠폰")
                .discount(1000)
                .quantity(5) // 총 수량 5
                .issued(5)   // 이미 전부 발급됨
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build()
        ).getId();

        // when & then
        mockMvc.perform(post("/v1/coupon/" + couponId + "/issue")
                        .header("userId", randomUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("쿠폰 발급 수량을 초과했습니다."));
    }

    @Test
    @DisplayName("성공: 만료된 쿠폰은 목록에서 제외된다")
    void get_coupons_excludes_expired_coupons() throws Exception {
        // given
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        // 유효한 쿠폰
        Coupon validCoupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("유효 쿠폰")
                .discount(1000)
                .quantity(10)
                .issued(1)
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build());

        couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(randomUserId)
                .couponId(validCoupon.getId())
                .state(0)
                .startAt(nowPlus(-1))
                .endAt(nowPlus(5))
                .createdAt(now())
                .updatedAt(now())
                .build());

        // 만료된 쿠폰
        Coupon expiredCoupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("만료 쿠폰")
                .discount(500)
                .quantity(10)
                .issued(1)
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build());

        couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(randomUserId)
                .couponId(expiredCoupon.getId())
                .state(0)
                .startAt(nowPlus(-10))
                .endAt(nowPlus(-1))  // 이미 기간 만료
                .createdAt(now())
                .updatedAt(now())
                .build());

        // when & then
        mockMvc.perform(get("/v1/coupon")
                        .header("userId", randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coupons").isArray())
                .andExpect(jsonPath("$.coupons.length()").value(1))
                .andExpect(jsonPath("$.coupons[0].couponId").value(validCoupon.getId()))
                .andExpect(jsonPath("$.coupons[0].description").value("유효 쿠폰"));
    }

    private String now() {
        return LocalDateTime.now().toString();
    }

    private String nowPlus(int days) {
        return LocalDateTime.now().plusDays(days).toString();
    }

}
