package kr.hhplus.be.server.domain.order.dto;

public record CreateOrderServiceRequest(
        Long userId,
        Long couponIssueId,
        Long totalPrice
) {
    public CreateOrderServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice는 0 이상이어야 합니다.");
        }
    }
}
