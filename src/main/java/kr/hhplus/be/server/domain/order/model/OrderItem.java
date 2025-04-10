package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.dto.CartItemResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Entity
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = true)
    private Long orderId;

    private Long productId;

    private Long optionId;

    private Long eachPrice;

    private Integer quantity;

    private String createdAt;

    private String updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private OrderOption orderOption;

    public Long calculateTotalPrice() {
        return eachPrice * quantity;
    }

    public OrderOption getOption(List<OrderOption> options) {
        return options.stream()
                .filter(option -> option.getId().equals(optionId)) // optionId로 필터링
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("옵션이 존재하지 않습니다."));
    }

    public int getStock(List<OrderOption> options) {
        return getOption(options).getStockQuantity();
    }

    public int getSize(List<OrderOption> options) {
        return getOption(options).getSize();
    }

    public static OrderItem of(
            Long userId,
            Long orderId,
            Long productId,
            Long optionId,
            Long eachPrice,
            Integer quantity
    ) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return OrderItem.builder()
                .userId(userId)
                .orderId(orderId)
                .productId(productId)
                .optionId(optionId)
                .eachPrice(eachPrice)
                .quantity(quantity)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public CartItemResponse toCartItemResponse() {
        if (orderOption == null) {
            throw new IllegalStateException("OrderOption이 초기화되지 않았습니다.");
        }
        int stock = orderOption.getStockQuantity();
        int size = orderOption.getSize();

        return new CartItemResponse(
                productId,
                quantity,
                optionId,
                eachPrice,
                stock,
                size
        );
    }
}
