package kr.hhplus.be.server.domain.product.event;

import java.util.List;

public record ProductSoldEvent(List<Long> productIds) {
}