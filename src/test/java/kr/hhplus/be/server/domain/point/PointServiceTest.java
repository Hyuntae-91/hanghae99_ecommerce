package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.common.exception.ResourceNotFoundException;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.application.point.dto.PointHistoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private PointService pointService;
    private PointRepository pointRepository;

    @BeforeEach
    void setUp() {
        pointRepository = mock(PointRepository.class);
        pointService = new PointService(pointRepository);
    }

    @Nested
    class GetPointTest {

        @Test
        @DisplayName("성공: 포인트 조회")
        void getPoint_success() {
            // given
            UserPoint userPoint = new UserPoint(1L, 500L);
            when(pointRepository.get(1L)).thenReturn(userPoint);

            // when
            Long result = pointService.getPoint(1L);

            // then
            assertThat(result).isEqualTo(500L);
        }

        @Test
        @DisplayName("실패: 유저가 존재하지 않으면 예외 발생")
        void getPoint_userNotFound() {
            // given
            when(pointRepository.get(1L)).thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointService.getPoint(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).get(1L);
            verifyNoMoreInteractions(pointRepository);
        }
    }

    @Nested
    class ChargePointTest {

        @Test
        @DisplayName("성공: 포인트 충전")
        void charge_success() {
            // given
            UserPoint userPoint = new UserPoint(1L, 100L);
            when(pointRepository.get(1L)).thenReturn(userPoint);

            // when
            Long total = pointService.charge(1L, 200L);

            // then
            assertThat(total).isEqualTo(300L);
            verify(pointRepository).savePoint(userPoint);
            verify(pointRepository).saveHistory(any(PointHistory.class));
        }

        @Test
        @DisplayName("실패: 유저 없음")
        void charge_userNotFound() {
            when(pointRepository.get(999999L)).thenThrow(new ResourceNotFoundException("User not found"));

            assertThatThrownBy(() -> pointService.charge(999999L, 100L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).get(999999L);
            verifyNoMoreInteractions(pointRepository);
        }

        @Test
        @DisplayName("실패: 충전 금액이 0 이하")
        void charge_invalidAmount() {
            UserPoint userPoint = new UserPoint(1L, 100L);
            when(pointRepository.get(1L)).thenReturn(userPoint);

            assertThatThrownBy(() -> pointService.charge(1L, -1L))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(pointRepository).get(1L);
            verifyNoMoreInteractions(pointRepository);
        }

        @Test
        @DisplayName("실패: 포인트 히스토리 저장 중 예외 발생")
        void charge_historySaveFails() {
            // given
            UserPoint userPoint = new UserPoint(1L, 100L);
            when(pointRepository.get(1L)).thenReturn(userPoint);
            doThrow(new RuntimeException("DB Error")).when(pointRepository).saveHistory(any());

            // when & then
            assertThatThrownBy(() -> pointService.charge(1L, 100L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB Error");

            verify(pointRepository).get(1L);
            verify(pointRepository).savePoint(userPoint);
            verify(pointRepository).saveHistory(any());
        }

        @Test
        @DisplayName("성공: 포인트 히스토리 조회")
        void getHistory_success() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 10;
            String sort = "createdAt";
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort));

            List<PointHistory> mockHistories = List.of(
                    new PointHistory(1L, userId, 100L, PointHistoryType.CHARGE, "2024-04-01 12:00:00"),
                    new PointHistory(2L, userId, 50L, PointHistoryType.USE, "2024-04-02 13:00:00")
            );
            when(pointRepository.getHistory(userId, pageable)).thenReturn(mockHistories);

            // when
            List<PointHistoryDto> result = pointService.getHistory(userId, page, size, sort);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).type()).isEqualTo(PointHistoryType.CHARGE.name());
            verify(pointRepository).getHistory(userId, pageable);
        }

        @Test
        @DisplayName("성공: 포인트 히스토리가 비어있을 경우")
        void getHistory_empty() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 10;
            String sort = "createdAt";
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort));

            when(pointRepository.getHistory(userId, pageable)).thenReturn(Collections.emptyList());

            // when
            List<PointHistoryDto> result = pointService.getHistory(userId, page, size, sort);

            // then
            assertThat(result).isEmpty();
            verify(pointRepository).getHistory(userId, pageable);
        }
        @Test
        @DisplayName("실패: 유저가 존재하지 않으면 예외 발생")
        void getHistory_userNotFound() {
            // given
            Long userId = 999L;
            int page = 1;
            int size = 10;
            String sort = "createdAt";
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort));

            when(pointRepository.getHistory(userId, pageable))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointService.getHistory(userId, page, size, sort))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointRepository).getHistory(userId, pageable);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 필드로 정렬 시 예외 발생")
        void getHistory_invalidSortField() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 10;
            String invalidSortField = "invalidField";
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, invalidSortField));

            when(pointRepository.getHistory(userId, pageable))
                    .thenThrow(new IllegalArgumentException("Invalid sort field"));

            // when & then
            assertThatThrownBy(() -> pointService.getHistory(userId, page, size, invalidSortField))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid sort field");

            verify(pointRepository).getHistory(eq(userId), any(Pageable.class));
        }

        @Test
        @DisplayName("실패: 페이지 번호가 음수이면 예외 발생")
        void getHistory_invalidPageNumber() {
            Long userId = 1L;
            int invalidPage = -1;
            int size = 10;
            String sort = "createdAt";

            assertThatThrownBy(() -> pointService.getHistory(userId, invalidPage, size, sort))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page index must not be less than zero");

            verifyNoInteractions(pointRepository);
        }

        @Test
        @DisplayName("실패: 페이지 사이즈가 0이면 예외 발생")
        void getHistory_invalidPageSize() {
            Long userId = 1L;
            int page = 1;
            int invalidSize = 0;
            String sort = "createdAt";

            assertThatThrownBy(() -> pointService.getHistory(userId, page, invalidSize, sort))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must not be less than one");

            verifyNoInteractions(pointRepository);
        }

    }
}
