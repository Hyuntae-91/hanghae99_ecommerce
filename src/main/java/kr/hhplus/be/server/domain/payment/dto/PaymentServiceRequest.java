package kr.hhplus.be.server.domain.payment.dto;

import java.util.List;

public record PaymentServiceRequest (
        Long userId,
        Long totalPrice,
        Long couponIssueId,
        List<PaymentOrderItemDto> orderItems
) {
    public PaymentServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice는 0 이상이어야 합니다.");
        }
        if (couponIssueId != null && couponIssueId < 1) {
            throw new IllegalArgumentException("couponIssueId는 1 이상이어야 합니다.");
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("orderItems는 null이거나 비어 있을 수 없습니다.");
        }
    }
}