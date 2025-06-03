package kr.hhplus.be.server.domain.order.dto.request;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record CreateOrderServiceRequest(
        Long userId,
        Long couponId,
        Long couponIssueId,
        List<CreateOrderOptionDto> options
) {
    public CreateOrderServiceRequest {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("orders 는 비어 있을 수 없습니다.");
        }
    }

    public List<Long> extractOptionIds() {
        return options.stream()
                .map(CreateOrderOptionDto::optionId)
                .toList();
    }

    public Map<Long, Integer> toQuantityMap() {
        return options.stream()
                .collect(Collectors.toMap(CreateOrderOptionDto::optionId, CreateOrderOptionDto::quantity));
    }
}
