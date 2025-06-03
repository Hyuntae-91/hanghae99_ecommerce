package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.common.constants.Groups;
import kr.hhplus.be.server.common.constants.Topics;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Container
    static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("test.kafka.topic", () -> Topics.COUPON_ISSUE_TOPIC);
        registry.add("test.kafka.group", () -> Groups.COUPON_ISSUE_GROUP);
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
    private CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    private CouponRedisRepository couponRedisRepository;

    @Test
    @DisplayName("성공: 쿠폰 발급 후 Redis 기록 확인")
    void issue_coupon_success_with_redis_only() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long randomCouponId = ThreadLocalRandom.current().nextInt(1, 100_000);

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .id(randomCouponId)
                .type(CouponType.FIXED)
                .description("테스트 쿠폰")
                .discount(1000)
                .quantity(10)
                .state(1)
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build());

        couponRedisRepository.saveCouponInfo(coupon.getId(), CouponJsonMapper.toCouponJson(coupon));
        couponRedisRepository.saveStock(coupon.getId(), coupon.getQuantity());

        Long couponId = coupon.getId();

        // when
        mockMvc.perform(post("/v1/coupon/{couponId}/issue", couponId)
                        .header("userId", randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponId").value(couponId))
                .andExpect(jsonPath("$.type").value("FIXED"))
                .andExpect(jsonPath("$.discount").value(1000));

        // then
        Map<Long, CouponIssueRedisDto> issuedCoupons = couponRedisRepository.findAllIssuedCoupons(randomUserId);
        CouponIssueRedisDto dto = issuedCoupons.get(couponId);

        assertThat(dto).isNotNull();
        assertThat(dto.getUse()).isEqualTo(0);

        List<CouponIssue> result = couponIssueJpaRepository.findAllByUserId(randomUserId);
        System.out.println(result);
    }

    @Test
    @DisplayName("성공: 쿠폰 목록 조회")
    void get_coupons_success() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long randomCouponId = ThreadLocalRandom.current().nextInt(1, 100_000);

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .id(randomCouponId)
                .type(CouponType.FIXED)
                .description("조회용 쿠폰")
                .discount(1000)
                .quantity(10)
                .state(1)
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build());

        couponRedisRepository.saveCouponInfo(coupon.getId(), CouponJsonMapper.toCouponJson(coupon));
        couponRedisRepository.saveStock(coupon.getId(), coupon.getQuantity());
        couponRedisRepository.addCouponForUser(randomUserId, coupon.getId(), 1L, 0);

        mockMvc.perform(get("/v1/coupon")
                        .header("userId", randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coupons").isArray())
                .andExpect(jsonPath("$.coupons.length()").value(1))
                .andExpect(jsonPath("$.coupons[0].id").value(coupon.getId()))
                .andExpect(jsonPath("$.coupons[0].discount").value(1000));
    }

    @Test
    @DisplayName("실패: 쿠폰 수량 초과로 발급 실패")
    void issue_coupon_fail_when_quantity_exceeded() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long randomCouponId = ThreadLocalRandom.current().nextInt(1, 100_000);

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .id(randomCouponId)
                .type(CouponType.FIXED)
                .description("초과 쿠폰")
                .discount(1000)
                .quantity(0)
                .state(1)
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build());

        couponRedisRepository.saveCouponInfo(coupon.getId(), CouponJsonMapper.toCouponJson(coupon));
        couponRedisRepository.saveStock(coupon.getId(), 0);

        mockMvc.perform(post("/v1/coupon/{couponId}/issue", coupon.getId())
                        .header("userId", randomUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("쿠폰 소진"));
    }

    @Test
    @DisplayName("성공: 만료된 쿠폰은 목록에서 제외된다")
    void get_coupons_excludes_expired_coupons() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long randomCouponId = ThreadLocalRandom.current().nextInt(1, 100_000);

        Coupon validCoupon = couponJpaRepository.save(Coupon.builder()
                .id(randomCouponId)
                .type(CouponType.FIXED)
                .description("유효 쿠폰")
                .discount(1000)
                .quantity(10)
                .state(1)
                .expirationDays(7)
                .createdAt(now())
                .updatedAt(now())
                .build());

        couponRedisRepository.saveCouponInfo(validCoupon.getId(), CouponJsonMapper.toCouponJson(validCoupon));
        couponRedisRepository.saveStock(validCoupon.getId(), validCoupon.getQuantity());
        couponRedisRepository.addCouponForUser(randomUserId, validCoupon.getId(), 1L,0);

        mockMvc.perform(get("/v1/coupon")
                        .header("userId", randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coupons").isArray())
                .andExpect(jsonPath("$.coupons.length()").value(1))
                .andExpect(jsonPath("$.coupons[0].id").value(validCoupon.getId()))
                .andExpect(jsonPath("$.coupons[0].description").value("유효 쿠폰"));
    }

    private String now() {
        return LocalDateTime.now().toString();
    }
}
