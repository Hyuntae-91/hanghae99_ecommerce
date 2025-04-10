package kr.hhplus.be.server.domain.point.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Table(name = "user_point")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserPoint {
    @Id
    @Column(name = "id")
    private Long userId;

    @Column(nullable = false)
    private Long point;

    public void charge(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0 이상이어야 합니다.");
        }
        this.point += amount;
    }

    public void use(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0 이상이어야 합니다.");
        }
        if (point - amount < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        this.point -= amount;
    }

    public void validateUsableBalance(long amount) {
        if (this.point < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
    }
}
