package kr.hhplus.be.server.dto.order;

public record OrderRequest(
        Long productId,
        int quantity,
        Long couponIssueId // 0 또는 null 허용
) {}
