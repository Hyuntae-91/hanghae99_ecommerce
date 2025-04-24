package kr.hhplus.be.server.domain.coupon.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.coupon.dto.response.CouponIssueDto;
import kr.hhplus.be.server.exception.custom.InvalidCouponUseException;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coupon_issue")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Integer state;

    @Column(name = "start_at", nullable = false)
    private String startAt;

    @Column(name = "end_at", nullable = false)
    private String endAt;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public void updateState(int state) {
        this.state = state;
        this.updatedAt = java.time.LocalDateTime.now().toString(); // 업데이트 시간도 갱신
    }

    public void validateUsable() {
        if (this.state != 0) {
            throw new InvalidCouponUseException("사용할 수 없는 쿠폰입니다.");
        }

        String now = java.time.LocalDateTime.now().toString();
        if (now.compareTo(this.startAt) < 0 || now.compareTo(this.endAt) > 0) {
            throw new InvalidCouponUseException("쿠폰 사용 가능 기간이 아닙니다.");
        }
    }

    public long calculateFinalPrice(long originalPrice, Coupon coupon) {
        long discountAmount = switch (coupon.getType()) {
            case FIXED -> coupon.getDiscount();
            case PERCENT -> originalPrice * coupon.getDiscount() / 100;
            default -> throw new InvalidCouponUseException("지원하지 않는 쿠폰 타입입니다.");
        };

        discountAmount = Math.min(discountAmount, originalPrice);
        return originalPrice - discountAmount;
    }

    public void markUsed() {
        this.state = 1;
        this.updatedAt = LocalDateTime.now().toString();
    }

    public CouponIssueDto toDto(Coupon coupon) {
        return new CouponIssueDto(
                couponId,
                coupon.getType().name(),
                coupon.getDescription(),
                coupon.getDiscount(),
                state,
                startAt,
                endAt,
                createdAt
        );
    }

    public static CouponIssue of(
            Long id,
            Long userId,
            Long couponId,
            Integer state,
            String startAt,
            String endAt,
            String createdAt,
            String updatedAt
    ) {
        return new CouponIssue(
                id,
                userId,
                couponId,
                state,
                startAt,
                endAt,
                0L,
                createdAt,
                updatedAt
        );
    }

}
