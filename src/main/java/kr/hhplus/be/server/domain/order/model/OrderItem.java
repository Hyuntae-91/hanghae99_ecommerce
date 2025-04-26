package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.dto.response.CartItemResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Entity
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_option_id")
    private Long optionId;

    private Long eachPrice;

    private Integer quantity;

    private String createdAt;

    private String updatedAt;

    public Long calculateTotalPrice() {
        return eachPrice * quantity;
    }

    public void applyOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void applyQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    public static OrderItem of(
            Long userId,
            Long productId,
            Long optionId,
            Long eachPrice,
            Integer quantity
    ) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return OrderItem.builder()
                .userId(userId)
                .productId(productId)
                .optionId(optionId)
                .eachPrice(eachPrice)
                .quantity(quantity)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public CartItemResponse toCartItemResponse(OrderOption option) {
        return new CartItemResponse(
                this.productId,
                this.quantity,
                this.optionId,
                this.eachPrice,
                option.getStockQuantity(),
                option.getSize()
        );
    }
}
