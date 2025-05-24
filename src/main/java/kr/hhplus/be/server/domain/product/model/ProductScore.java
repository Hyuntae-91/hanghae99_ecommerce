package kr.hhplus.be.server.domain.product.model;

public record  ProductScore(Long productId, double score) {

    private static final double DECAY_FACTOR = 0.5;
    private static final double SCORE_THRESHOLD = 30.0;
    public static final double SALE_SCORE = 5.0;

    public boolean isBelowThreshold() {
        return this.score <= SCORE_THRESHOLD;
    }

    public ProductScore decay() {
        return new ProductScore(productId, this.score * DECAY_FACTOR);
    }
}
