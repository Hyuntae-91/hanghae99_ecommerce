package kr.hhplus.be.server.domain.point;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.point.model.PointHistory;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.application.point.PointHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    public Long getPoint(Long userId) {
        UserPoint userPoint = pointRepository.get(userId);
        return userPoint.getPoint();
    }

    public List<PointHistoryDto> getHistory(Long userId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort));
        List<PointHistory> historyList = pointRepository.getHistory(userId, pageable);

        return historyList.stream()
            .map(PointHistoryDto::from)
            .toList();
    }

    @Transactional
    public Long charge(Long userId, Long amount) {
        UserPoint userPoint = pointRepository.get(userId);
        userPoint.charge(amount);
        pointRepository.savePoint(userPoint);

        PointHistory history = PointHistory.builder()
                .userId(userId)
                .point(amount)
                .type(PointHistoryType.CHARGE)
                .createdAt(LocalDateTime.now().toString())
                .build();
        pointRepository.saveHistory(history);

        return userPoint.getPoint();
    }
}
