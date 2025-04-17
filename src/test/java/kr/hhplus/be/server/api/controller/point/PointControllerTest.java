package kr.hhplus.be.server.api.controller.point;

import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.infrastructure.point.repository.PointHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserPointJpaRepository userPointJpaRepository;

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Test
    @DisplayName("성공: 포인트 조회")
    void get_point_success() throws Exception {
        Long userId = 1L;
        Long point = 100000L;

        UserPoint userPoint = UserPoint.builder()
                .userId(userId)
                .point(point)
                .build();
        userPointJpaRepository.save(userPoint);

        mockMvc.perform(get("/v1/point")
                        .header("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(point));

    }

    @Test
    @DisplayName("성공: 포인트 충전")
    void put_point_charge_success() throws Exception {
        Long userId = 1L;
        Long point = 100000L;
        Long chargeAmount = 500L;

        UserPoint userPoint = UserPoint.builder()
                .userId(userId)
                .point(point)
                .build();
        userPointJpaRepository.save(userPoint);

        String payload = String.format("""
            {
              "point": %d
            }
        """, chargeAmount);

        mockMvc.perform(put("/v1/point")
                        .header("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoint").value(point + chargeAmount));

    }

    @Test
    @DisplayName("성공: 포인트 히스토리 조회")
    void get_point_history_success() throws Exception {
        // given
        Long userId = 1L;

        // 포인트 및 히스토리 저장
        UserPoint userPoint = UserPoint.builder()
                .userId(userId)
                .point(1000L)
                .build();
        userPointJpaRepository.save(userPoint);

        pointHistoryJpaRepository.save(PointHistory.of(userId, 100L, PointHistoryType.CHARGE));
        pointHistoryJpaRepository.save(PointHistory.of(userId, 50L, PointHistoryType.USE));

        // when & then
        mockMvc.perform(get("/v1/point/history")
                        .header("userId", userId)
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

        String payload = String.format("""
            {
              "point": %d
            }
        """, chargeAmount);

        mockMvc.perform(put("/v1/point")
                        .header("userId", 1)
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
        String payload = """
            {
              "point": -500
            }
        """;

        mockMvc.perform(put("/v1/point")
                        .header("userId", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

}
