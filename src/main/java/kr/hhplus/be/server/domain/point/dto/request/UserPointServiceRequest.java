package kr.hhplus.be.server.domain.point.dto.request;

public record UserPointServiceRequest(
        Long userId
) {
    public UserPointServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
    }
}
