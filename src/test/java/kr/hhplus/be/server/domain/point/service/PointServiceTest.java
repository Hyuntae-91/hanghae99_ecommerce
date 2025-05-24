package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.dto.request.PointUseServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.PointValidateUsableRequest;
import kr.hhplus.be.server.exception.custom.PointNotEnoughException;
import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.mapper.UserPointMapper;
import kr.hhplus.be.server.domain.point.dto.request.PointChargeServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.PointChargeServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private PointService pointService;
    private PointRepository pointRepository;
    private PointHistoryRepository pointHistoryRepository;
    private UserPointMapper userPointMapper;

    @BeforeEach
    void setUp() {
        pointRepository = mock(PointRepository.class);
        pointHistoryRepository = mock(PointHistoryRepository.class);
        userPointMapper = mock(UserPointMapper.class);
        pointService = new PointService(pointRepository, pointHistoryRepository, userPointMapper);
    }

    @Nested
    class GetPointTest {

        @Test
        @DisplayName("성공: 포인트 조회")
        void getPoint_success() {
            // given
            Long userId = 1L;
            UserPoint userPoint = UserPoint.of(userId, 500L);
            when(pointRepository.findWithLockByUserId(any())).thenReturn(userPoint);
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
            when(pointRepository.findWithLockByUserId(any()))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointService.getUserPoint(new UserPointServiceRequest(userId)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).findWithLockByUserId(any());
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

            UserPoint userPoint = UserPoint.of(userId, currentPoint);

            when(pointRepository.findWithLockByUserId(any())).thenReturn(userPoint);
            doNothing().when(pointHistoryRepository)
                    .saveHistory(eq(userId), eq(chargeAmount), eq(PointHistoryType.CHARGE));
            when(userPointMapper.toUserPointChargeResponse(userPoint)).thenReturn(
                    UserPointMapper.INSTANCE.toUserPointChargeResponse(
                            UserPoint.of(userId, currentPoint + chargeAmount)
                    )
            );

            // when
            PointChargeServiceResponse result = pointService.charge(
                    new PointChargeServiceRequest(userId, chargeAmount)
            );

            // then
            assertThat(result.point()).isEqualTo(currentPoint + chargeAmount);
            verify(pointRepository).savePoint(userPoint);
            verify(pointHistoryRepository).saveHistory(userId, chargeAmount, PointHistoryType.CHARGE);
        }

        @Test
        @DisplayName("실패: 유저 없음")
        void charge_userNotFound() {
            // given
            Long userId = 999999L;
            when(pointRepository.findWithLockByUserId(any()))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointService.charge(new PointChargeServiceRequest(userId, 100L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).findWithLockByUserId(any());
            verifyNoMoreInteractions(pointRepository);
        }

        @Test
        @DisplayName("실패: 포인트 히스토리 저장 중 예외 발생")
        void charge_historySaveFails() {
            // given
            Long userId = 1L;
            Long chargeAmount = 100L;
            UserPoint userPoint = UserPoint.of(userId, 100L);
            when(pointRepository.findWithLockByUserId(any())).thenReturn(userPoint);
            doThrow(new RuntimeException("DB Error")).when(pointHistoryRepository).saveHistory(
                    userId, chargeAmount, PointHistoryType.CHARGE
            );

            // when & then
            assertThatThrownBy(() -> pointService.charge(new PointChargeServiceRequest(userId, chargeAmount)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB Error");

            verify(pointRepository).findWithLockByUserId(any());
            verify(pointRepository).savePoint(userPoint);
            verify(pointHistoryRepository).saveHistory(userId, chargeAmount, PointHistoryType.CHARGE);
        }

        @Test
        @DisplayName("실패: 포인트 0 이하 충전 시 예외 발생")
        void charge_invalidPoint() {
            Long userId = 1L;

            assertThatThrownBy(() ->
                    pointService.charge(new PointChargeServiceRequest(userId, 0L))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("충전 포인트는 0보다 커야 합니다.");
        }
    }

    @Nested
    class UsePointTest {

        @Test
        @DisplayName("성공: 포인트 사용")
        void use_success() {
            // given
            Long userId = 1L;
            Long initialPoint = 1000L;
            Long useAmount = 300L;

            UserPoint userPoint = UserPoint.of(userId, initialPoint);
            when(pointRepository.findWithLockByUserId(any())).thenReturn(userPoint);
            doNothing().when(pointHistoryRepository).saveHistory(userId, useAmount, PointHistoryType.USE);
            when(userPointMapper.toUserPointUseResponse(userPoint)).thenReturn(
                    UserPointMapper.INSTANCE.toUserPointUseResponse(
                            UserPoint.of(userId, initialPoint - useAmount)
                    )
            );

            // when
            var response = pointService.use(new PointUseServiceRequest(userId, useAmount));

            // then
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.point()).isEqualTo(initialPoint - useAmount);
            verify(pointRepository).savePoint(userPoint);
            verify(pointHistoryRepository).saveHistory(userId, useAmount, PointHistoryType.USE);
        }

        @Test
        @DisplayName("실패: 유저 없음")
        void use_userNotFound() {
            // given
            when(pointRepository.findWithLockByUserId(any()))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointService.use(new PointUseServiceRequest(999L, 100L)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).findWithLockByUserId(any());
            verifyNoMoreInteractions(pointRepository);
        }

        @Test
        @DisplayName("실패: 포인트 부족")
        void use_pointNotEnough() {
            // given
            Long userId = 1L;
            Long initialPoint = 100L;
            Long useAmount = 200L;

            UserPoint userPoint = UserPoint.of(userId, initialPoint);
            when(pointRepository.findWithLockByUserId(any())).thenReturn(userPoint);

            // when & then
            assertThatThrownBy(() -> pointService.use(new PointUseServiceRequest(userId, useAmount)))
                    .isInstanceOf(PointNotEnoughException.class)
                    .hasMessageContaining("포인트가 부족합니다.");
        }
    }

    @Nested
    class ValidateUsableTest {

        @Test
        @DisplayName("성공: 포인트 충분하면 검증 통과")
        void validateUsable_success() {
            // given
            Long userId = 1L;
            Long totalPrice = 500L;
            UserPoint userPoint = UserPoint.of(userId, 1000L);

            when(pointRepository.get(any())).thenReturn(userPoint);

            // when & then
            pointService.validateUsable(new PointValidateUsableRequest(userId, totalPrice));
            verify(pointRepository).get(userId);
        }

        @Test
        @DisplayName("실패: 포인트 부족하면 예외 발생")
        void validateUsable_fail() {
            // given
            Long userId = 1L;
            Long totalPrice = 1500L;
            UserPoint userPoint = UserPoint.of(userId, 1000L);

            when(pointRepository.get(any())).thenReturn(userPoint);

            // when & then
            assertThatThrownBy(() -> pointService.validateUsable(new PointValidateUsableRequest(userId, totalPrice)))
                    .isInstanceOf(PointNotEnoughException.class)
                    .hasMessageContaining("포인트가 부족합니다.");
        }
    }
}
