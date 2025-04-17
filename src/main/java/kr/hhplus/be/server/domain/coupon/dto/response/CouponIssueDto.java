package kr.hhplus.be.server.domain.coupon.dto.response;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;

public record CouponIssueDto(
        Long couponId,
        String type,         // PERCENT or FIXED
        String description,
        Integer discount,
        Integer state,       // 0: 사용 가능, 기타: 상태 값
        String startAt,
        String endAt,
        String createdAt
) {
    public CouponIssueDto {
        if (couponId == null || couponId < 1) {
            throw new IllegalArgumentException("couponId는 1 이상이어야 합니다.");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type은 null이거나 비어 있을 수 없습니다.");
        }
        if (!type.equals("PERCENT") && !type.equals("FIXED")) {
            throw new IllegalArgumentException("type은 PERCENT 또는 FIXED 이어야 합니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description은 null이거나 비어 있을 수 없습니다.");
        }
        if (discount == null || discount < 0) {
            throw new IllegalArgumentException("discount는 0 이상이어야 합니다.");
        }
        if (state == null || (state != 0 && state != 1 && state != -1)) {
            throw new IllegalArgumentException("state는 0, 1, -1 중 하나여야 합니다.");
        }
        if (startAt == null || startAt.isBlank()) {
            throw new IllegalArgumentException("startAt은 null이거나 비어 있을 수 없습니다.");
        }
        if (endAt == null || endAt.isBlank()) {
            throw new IllegalArgumentException("endAt은 null이거나 비어 있을 수 없습니다.");
        }
        if (createdAt == null || createdAt.isBlank()) {
            throw new IllegalArgumentException("createdAt은 null이거나 비어 있을 수 없습니다.");
        }
    }

    public static CouponIssueDto from(CouponIssue issue, Coupon coupon) {
        return new CouponIssueDto(
                coupon.getId(),
                coupon.getType().name(),
                coupon.getDescription(),
                coupon.getDiscount(),
                issue.getState(),
                issue.getStartAt(),
                issue.getEndAt(),
                issue.getCreatedAt()
        );
    }
}