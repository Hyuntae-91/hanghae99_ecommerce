package kr.hhplus.be.server.domain.product.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import kr.hhplus.be.server.domain.order.model.OrderOption;

import java.io.Serializable;

public record ProductOptionResponse(
        Long optionId,
        int size,
        int stock
) implements Serializable {
    @JsonCreator
    public ProductOptionResponse {
        if (optionId == null || optionId < 1) {
            throw new IllegalArgumentException("optionId는 1 이상이어야 합니다.");
        }
        if (size < 0) {
            throw new IllegalArgumentException("size는 0 이상이어야 합니다.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock은 0 이상이어야 합니다.");
        }
    }

    public static ProductOptionResponse from(OrderOption option) {
        return new ProductOptionResponse(
                option.getId(),
                option.getSize(),
                option.getStockQuantity()
        );
    }
}