package kr.hhplus.be.server.domain.point.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Table(name = "point_history")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long point;

    @Enumerated(EnumType.STRING)
    private PointHistoryType type;

    private String createdAt;

    public PointHistory(Long id, Long userId, Long point, PointHistoryType type, String createdAt) {
        if (type == null) {
            throw new NullPointerException("type cannot be null");
        }
        if (point == null || point < 0) {
            throw new IllegalArgumentException("point must be greater than or equal to 0");
        }
        this.id = id;
        this.userId = userId;
        this.point = point;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static PointHistory of(Long userId, Long point, PointHistoryType type) {
        String now = java.time.LocalDateTime.now().toString();
        return PointHistory.builder()
                .userId(userId)
                .point(point)
                .type(type)
                .createdAt(now)
                .build();
    }

}