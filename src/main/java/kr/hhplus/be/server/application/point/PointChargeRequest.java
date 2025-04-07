package kr.hhplus.be.server.application.point;

public record PointChargeRequest(
        Long point
) {
    public void validate() {
        if (point == null || point <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
    }
}
