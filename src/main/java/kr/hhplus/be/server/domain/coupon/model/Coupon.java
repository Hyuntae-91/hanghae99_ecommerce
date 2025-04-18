package kr.hhplus.be.server.domain.coupon.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer discount;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer issued;

    @Column(name = "expiration_days", nullable = false)
    private Integer expirationDays;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public void increaseIssued() {
        validateIssuable();
        this.issued += 1;
        this.updatedAt = java.time.LocalDateTime.now().toString();
    }

    public void validateIssuable() {
        if (this.issued >= this.quantity) {
            throw new IllegalStateException("쿠폰 발급 수량을 초과했습니다.");
        }
    }
}