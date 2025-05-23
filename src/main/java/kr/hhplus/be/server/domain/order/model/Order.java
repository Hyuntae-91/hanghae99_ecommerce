package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Table(name = "`order`")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int state; // 0: 생성, -1: 취소

    private Long totalPrice;

    private Long couponIssueId;

    private String createdAt;

    private String updatedAt;

    public static Order create(Long userId, Long totalPrice, Long couponIssueId) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (userId == null || totalPrice == null) {
            throw new IllegalArgumentException("userId와 totalPrice는 필수 값입니다.");
        }

        return Order.builder()
                .userId(userId)
                .totalPrice(totalPrice)
                .couponIssueId(couponIssueId)
                .state(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void cancel() {
        this.state = -1;
    }

    public void applyTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void applyCoupon(Long couponIssueId) {
        this.couponIssueId = couponIssueId;
    }

    public static Order of(Long userId, Long couponIssueId, Long totalPrice, int state) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return Order.builder()
                .userId(userId)
                .couponIssueId(couponIssueId)
                .totalPrice(totalPrice)
                .state(state)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

}
