package kr.hhplus.be.server.domain.product.dto.event;

import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;

import java.util.List;

public record ProductTotalPriceRequestedEvent(
        List<ProductOptionKeyDto> items
) {
    public ProductTotalPriceRequestedEvent {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items는 null이거나 비어 있을 수 없습니다.");
        }
    }
}
