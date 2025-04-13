package kr.hhplus.be.server.interfaces.api.point.dto.response;

import kr.hhplus.be.server.domain.point.dto.response.PointHistoryServiceResponse;

import java.util.List;

public record PointHistoryResponse(
        List<PointHistory> history
) {
    public static PointHistoryResponse from(List<PointHistoryServiceResponse> serviceResponses) {
        if (serviceResponses == null) {
            throw new IllegalArgumentException("history는 null일 수 없습니다.");
        }

        List<PointHistory> mapped = serviceResponses.stream()
                .map(PointHistory::from)
                .toList();

        return new PointHistoryResponse(mapped);
    }
}
