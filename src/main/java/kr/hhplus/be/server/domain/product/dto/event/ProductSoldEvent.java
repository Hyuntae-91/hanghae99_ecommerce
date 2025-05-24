package kr.hhplus.be.server.domain.product.dto.event;

import java.util.List;

public record ProductSoldEvent(List<Long> productIds) {
}