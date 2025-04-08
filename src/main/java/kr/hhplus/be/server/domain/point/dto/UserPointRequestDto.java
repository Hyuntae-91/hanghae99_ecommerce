package kr.hhplus.be.server.domain.point.dto;

public record UserPointRequestDto(
        Long userId
) {
    public UserPointRequestDto {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId는 1 이상이어야 합니다.");
        }
    }
}
