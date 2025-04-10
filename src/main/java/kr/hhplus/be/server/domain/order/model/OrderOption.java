package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.*;

@Getter
@Entity
@Table(name = "order_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer size;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @PrePersist
    @PreUpdate
    public void validateFields() {
        if (size == null || size < 0) {
            throw new IllegalArgumentException("size는 0 이상이어야 합니다.");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("stockQuantity는 0 이상이어야 합니다.");
        }
    }
}
