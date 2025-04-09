package kr.hhplus.be.server.domain.product.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long price;

    /**
     * 상품 상태 코드
     * -1: 삭제
     *  1: 판매중
     *  2: 품절
     *  3: 숨김
     */
    @Column(nullable = false)
    private Integer state;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @OneToMany(mappedBy = "productId", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "productId", fetch = FetchType.LAZY)
    private List<OrderOption> orderOptions = new ArrayList<>();

    public void setOrderOptions(List<OrderOption> orderOptions) {
        this.orderOptions = orderOptions != null ? orderOptions : new ArrayList<>();
    }

    public boolean isDeleted() {
        return state == -1;
    }

    public boolean isOnSale() {
        return state == 1;
    }

    public boolean isSoldOut() {
        return state == 2;
    }

    public boolean isHidden() {
        return state == 3;
    }
}
