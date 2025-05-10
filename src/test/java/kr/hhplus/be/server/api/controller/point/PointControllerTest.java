package kr.hhplus.be.server.api.controller.point;

import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.infrastructure.point.repository.PointHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
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

import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PointControllerTest {

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
    private UserPointJpaRepository userPointJpaRepository;

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Test
    @DisplayName("성공: 포인트 조회")
    void get_point_success() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        Long point = 100000L;

        UserPoint userPoint = UserPoint.builder()
                .userId(randomUserId)
                .point(point)
                .build();
        userPointJpaRepository.save(userPoint);

        mockMvc.perform(get("/v1/point")
                        .header("userId", randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(randomUserId))
                .andExpect(jsonPath("$.point").value(point));

    }

    @Test
    @DisplayName("성공: 포인트 충전")
    void put_point_charge_success() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        Long point = 100000L;
        Long chargeAmount = 500L;

        UserPoint userPoint = UserPoint.builder()
                .userId(randomUserId)
                .point(point)
                .build();
        userPointJpaRepository.save(userPoint);

        String payload = String.format("""
            {
              "point": %d
            }
        """, chargeAmount);

        mockMvc.perform(put("/v1/point")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoint").value(point + chargeAmount));

    }

    @Test
    @DisplayName("성공: 포인트 히스토리 조회")
    void get_point_history_success() throws Exception {
        // given
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        // 포인트 및 히스토리 저장
        UserPoint userPoint = UserPoint.builder()
                .userId(randomUserId)
                .point(1000L)
                .build();
        userPointJpaRepository.save(userPoint);

        pointHistoryJpaRepository.save(PointHistory.of(randomUserId, 100L, PointHistoryType.CHARGE));
        pointHistoryJpaRepository.save(PointHistory.of(randomUserId, 50L, PointHistoryType.USE));

        // when & then
        mockMvc.perform(get("/v1/point/history")
                        .header("userId", randomUserId)
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history.length()").value(2))
                .andExpect(jsonPath("$.history[0].type").exists())
                .andExpect(jsonPath("$.history[0].point").exists());
    }

    @Test
    @DisplayName("실패: 포인트 충전 0보다 커야한다")
    void put_point_charge_fail() throws Exception {
        Long chargeAmount = 0L;
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        String payload = String.format("""
            {
              "point": %d
            }
        """, chargeAmount);

        mockMvc.perform(put("/v1/point")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저의 포인트 조회 시 404 반환")
    void get_point_fail_when_user_not_found() throws Exception {
        mockMvc.perform(get("/v1/point")
                        .header("userId", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저의 포인트 히스토리 조회 시 404 반환")
    void get_point_history_fail_when_user_not_found() throws Exception {
        mockMvc.perform(get("/v1/point/history")
                        .header("userId", 9999)
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "createdAt"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("실패: 포인트 충전 금액이 음수일 경우 400 반환")
    void put_point_charge_fail_negative_amount() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        String payload = """
            {
              "point": -500
            }
        """;

        mockMvc.perform(put("/v1/point")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

}
