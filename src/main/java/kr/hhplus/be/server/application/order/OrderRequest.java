package kr.hhplus.be.server.application.order;

public record OrderRequest(
        Long productId,
        int quantity,
        Long couponIssueId // 0 또는 null 허용
) {}
