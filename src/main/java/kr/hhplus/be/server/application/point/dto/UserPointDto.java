package kr.hhplus.be.server.application.point.dto;

import kr.hhplus.be.server.application.product.dto.ProductDto;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import lombok.Builder;

@Builder
public record UserPointDto (
    Long userId,
    Long point
) {

    public static UserPointDto from(UserPoint userPoint) {
        return UserPointDto.builder()
                .userId(userPoint.getUserId())
                .point(userPoint.getPoint())
                .build();
    }
}
