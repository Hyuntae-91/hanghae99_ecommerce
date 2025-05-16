package kr.hhplus.be.server.domain.coupon.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.exception.custom.InvalidCouponUseException;
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
    private Integer state;

    @Column(name = "expiration_days", nullable = false)
    private Integer expirationDays;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public long calculateDiscount(long originalPrice) {
        return switch (type) {
            case FIXED -> Math.min(discount, originalPrice);
            case PERCENT -> Math.min(originalPrice * discount / 100, originalPrice);
        };
    }
}