package kr.hhplus.be.server.domain.payment.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Integer status;  // 1 = 결제 완료, -1 = 결제 취소 등

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public static Payment of(Long orderId, Integer status, Long totalPrice) {
        String now = java.time.LocalDateTime.now().toString();
        return Payment.builder()
                .orderId(orderId)
                .status(status)
                .totalPrice(totalPrice)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void cancel() {
        this.status = -1;
        this.updatedAt = java.time.LocalDateTime.now().toString();
    }

    public boolean isCompleted() {
        return status == 1;
    }

    public boolean isCancelled() {
        return status == -1;
    }
}
