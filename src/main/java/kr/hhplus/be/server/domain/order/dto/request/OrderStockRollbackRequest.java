package kr.hhplus.be.server.domain.order.dto.request;

import java.util.List;

public record OrderStockRollbackRequest (
        List<ProductInfoDataIds> items
){
    public OrderStockRollbackRequest {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items는 null이거나 비어 있을 수 없습니다.");
        }
    }
}
