package kr.hhplus.be.server.domain.point.dto.response;

import kr.hhplus.be.server.domain.point.model.UserPoint;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record UserPointServiceResponse(
    Long userId,
    Long point
) implements Serializable {
    public UserPointServiceResponse {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (point == null || point < 0) {
            throw new IllegalArgumentException("point는 0 이상이어야 합니다.");
        }
    }

    public static UserPointServiceResponse from(UserPoint userPoint) {
        return UserPointServiceResponse.builder()
                .userId(userPoint.getUserId())
                .point(userPoint.getPoint())
                .build();
    }
}
