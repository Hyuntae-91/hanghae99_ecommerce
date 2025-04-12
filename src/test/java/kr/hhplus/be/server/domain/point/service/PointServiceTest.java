package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.common.exception.ResourceNotFoundException;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.dto.UserPointMapper;
import kr.hhplus.be.server.domain.point.dto.request.PointChargeServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.PointHistoryServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.PointChargeServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.PointHistoryServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private PointService pointService;
    private PointRepository pointRepository;
    private UserPointMapper userPointMapper;

    @BeforeEach
    void setUp() {
        pointRepository = mock(PointRepository.class);
        userPointMapper = mock(UserPointMapper.class);
        pointService = new PointService(pointRepository, userPointMapper);
    }

    @Nested
    class GetPointTest {

        @Test
        @DisplayName("성공: 포인트 조회")
        void getPoint_success() {
            // given
            Long userId = 1L;
            UserPoint userPoint = new UserPoint(userId, 500L);
            when(pointRepository.get(any())).thenReturn(userPoint);
            when(userPointMapper.toUserPointResponse(userPoint)).thenReturn(
                    UserPointMapper.INSTANCE.toUserPointResponse(userPoint)
            );

            // when
            UserPointServiceResponse result = pointService.getUserPoint(new UserPointServiceRequest(userId));

            // then
            assertThat(result.point()).isEqualTo(500L);
            assertThat(result.userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("실패: 유저가 존재하지 않으면 예외 발생")
        void getPoint_userNotFound() {
            // given
            Long userId = 1L;
            when(pointRepository.get(any()))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointService.getUserPoint(new UserPointServiceRequest(userId)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).get(any());
            verifyNoMoreInteractions(pointRepository);
        }
    }

    @Nested
    class ChargePointTest {

        @Test
        @DisplayName("성공: 포인트 충전")
        void charge_success() {
            // given
            Long userId = 1L;
            Long currentPoint = 100L;
            Long chargeAmount = 200L;

            UserPoint userPoint = new UserPoint(userId, currentPoint);
            when(pointRepository.get(any())).thenReturn(userPoint);
            when(userPointMapper.toUserPointChargeResponse(userPoint)).thenReturn(
                    UserPointMapper.INSTANCE.toUserPointChargeResponse(
                            new UserPoint(userId, currentPoint + chargeAmount)
                    )
            );

            // when
            PointChargeServiceResponse result = pointService.charge(new PointChargeServiceRequest(userId, chargeAmount));

            // then
            assertThat(result.point()).isEqualTo(currentPoint + chargeAmount);
            verify(pointRepository).savePoint(userPoint);
            verify(pointRepository).saveHistory(userId, chargeAmount, PointHistoryType.CHARGE);
        }

        @Test
        @DisplayName("실패: 유저 없음")
        void charge_userNotFound() {
            // given
            Long userId = 999999L;
            when(pointRepository.get(any()))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointService.charge(new PointChargeServiceRequest(userId, 100L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).get(any());
            verifyNoMoreInteractions(pointRepository);
        }

        @Test
        @DisplayName("실패: 포인트 히스토리 저장 중 예외 발생")
        void charge_historySaveFails() {
            // given
            Long userId = 1L;
            Long chargeAmount = 100L;
            UserPoint userPoint = new UserPoint(userId, 100L);
            when(pointRepository.get(any())).thenReturn(userPoint);
            doThrow(new RuntimeException("DB Error")).when(pointRepository).saveHistory(
                    userId, chargeAmount, PointHistoryType.CHARGE
            );

            // when & then
            assertThatThrownBy(() -> pointService.charge(new PointChargeServiceRequest(userId, chargeAmount)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB Error");

            verify(pointRepository).get(any());
            verify(pointRepository).savePoint(userPoint);
            verify(pointRepository).saveHistory(userId, chargeAmount, PointHistoryType.CHARGE);
        }
    }

    @Nested
    class GetHistoryTest {

        @Test
        @DisplayName("성공: 포인트 히스토리 조회")
        void getHistory_success() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 10;
            String sort = "createdAt";

            List<PointHistory> mockHistories = List.of(
                    new PointHistory(1L, userId, 100L, PointHistoryType.CHARGE, "2024-04-01 12:00:00"),
                    new PointHistory(2L, userId, 50L, PointHistoryType.USE, "2024-04-02 13:00:00")
            );
            when(pointRepository.getHistory(userId, page, size, sort)).thenReturn(mockHistories);
            when(userPointMapper.toHistoryListResponse(mockHistories)).thenReturn(
                    UserPointMapper.INSTANCE.toHistoryListResponse(mockHistories)
            );

            // when
            List<PointHistoryServiceResponse> result = pointService.getHistory(
                    new PointHistoryServiceRequest(userId, page, size, sort)
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).type()).isEqualTo(PointHistoryType.CHARGE.name());
            verify(pointRepository).getHistory(userId, page, size, sort);
        }

        @Test
        @DisplayName("성공: 포인트 히스토리가 비어있을 경우")
        void getHistory_empty() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 10;
            String sort = "createdAt";

            when(pointRepository.getHistory(userId, page, size, sort)).thenReturn(Collections.emptyList());

            // when
            List<PointHistoryServiceResponse> result = pointService.getHistory(
                    new PointHistoryServiceRequest(userId, page, size, sort)
            );

            // then
            assertThat(result).isEmpty();
            verify(pointRepository).getHistory(userId, page, size, sort);
        }

        @Test
        @DisplayName("실패: 유저가 존재하지 않으면 예외 발생")
        void getHistory_userNotFound() {
            // given
            Long userId = 999L;
            int page = 1;
            int size = 10;
            String sort = "createdAt";

            when(pointRepository.getHistory(userId, page, size, sort))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointService.getHistory(
                    new PointHistoryServiceRequest(userId, page, size, sort)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).getHistory(userId, page, size, sort);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 필드로 정렬 시 예외 발생")
        void getHistory_invalidSortField() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 10;
            String sort = "invalidField";

            when(pointRepository.getHistory(userId, page, size, sort))
                    .thenThrow(new IllegalArgumentException("Invalid sort field"));

            // when & then
            assertThatThrownBy(() -> pointService.getHistory(
                    new PointHistoryServiceRequest(userId, page, size, sort)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid sort field");

            verify(pointRepository).getHistory(userId, page, size, sort);
        }
    }
}
