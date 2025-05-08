package kr.hhplus.be.server.domain.point.dto.request;

public record PointValidateUsableRequest(
        Long userId,
        Long totalPrice
) {
    public PointValidateUsableRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (totalPrice == null || totalPrice <= 0) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");
        }
    }
}
