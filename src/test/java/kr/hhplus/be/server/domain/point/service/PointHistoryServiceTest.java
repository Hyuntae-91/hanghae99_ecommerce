package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import kr.hhplus.be.server.domain.point.mapper.UserPointMapper;
import kr.hhplus.be.server.domain.point.dto.request.PointHistoryServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.PointHistoryServiceResponse;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class PointHistoryServiceTest {

    private PointRepository pointRepository;
    private PointHistoryService pointHistoryService;
    private PointHistoryRepository pointHistoryRepository;
    private UserPointMapper userPointMapper;

    @BeforeEach
    void setUp() {
        pointRepository = mock(PointRepository.class);
        pointHistoryRepository = mock(PointHistoryRepository.class);
        userPointMapper = mock(UserPointMapper.class);
        pointHistoryService = new PointHistoryService(pointRepository, pointHistoryRepository, userPointMapper);
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
            when(pointHistoryRepository.getHistory(userId, page, size, sort)).thenReturn(mockHistories);
            when(userPointMapper.toHistoryListResponse(mockHistories)).thenReturn(
                    UserPointMapper.INSTANCE.toHistoryListResponse(mockHistories)
            );

            // when
            List<PointHistoryServiceResponse> result = pointHistoryService.getHistory(
                    new PointHistoryServiceRequest(userId, page, size, sort)
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).type()).isEqualTo(PointHistoryType.CHARGE.name());
            verify(pointHistoryRepository).getHistory(userId, page, size, sort);
        }

        @Test
        @DisplayName("성공: 포인트 히스토리가 비어있을 경우")
        void getHistory_empty() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 10;
            String sort = "createdAt";

            when(pointHistoryRepository.getHistory(userId, page, size, sort)).thenReturn(Collections.emptyList());

            // when
            List<PointHistoryServiceResponse> result = pointHistoryService.getHistory(
                    new PointHistoryServiceRequest(userId, page, size, sort)
            );

            // then
            assertThat(result).isEmpty();
            verify(pointHistoryRepository).getHistory(userId, page, size, sort);
        }

        @Test
        @DisplayName("실패: 유저가 존재하지 않으면 예외 발생")
        void getHistory_userNotFound() {
            // given
            Long userId = 999L;
            int page = 1;
            int size = 10;
            String sort = "createdAt";

            when(pointHistoryRepository.getHistory(userId, page, size, sort))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            // when & then
            assertThatThrownBy(() -> pointHistoryService.getHistory(
                    new PointHistoryServiceRequest(userId, page, size, sort)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(pointHistoryRepository).getHistory(userId, page, size, sort);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 필드로 정렬 시 예외 발생")
        void getHistory_invalidSortField() {
            // given
            Long userId = 1L;
            int page = 1;
            int size = 10;
            String sort = "invalidField";

            when(pointHistoryRepository.getHistory(userId, page, size, sort))
                    .thenThrow(new IllegalArgumentException("Invalid sort field"));

            // when & then
            assertThatThrownBy(() -> pointHistoryService.getHistory(
                    new PointHistoryServiceRequest(userId, page, size, sort)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid sort field");

            verify(pointHistoryRepository).getHistory(userId, page, size, sort);
        }
    }
}
