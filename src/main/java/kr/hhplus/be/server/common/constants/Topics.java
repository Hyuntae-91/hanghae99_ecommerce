package kr.hhplus.be.server.common.constants;

public class Topics {
    public static final String MOCK_API_TOPIC = "mock-api-topic";

    public static final String COUPON_ISSUE_TOPIC = "coupon-issue-topic";
    public static final String COUPON_APPLY_COMPLETE_TOPIC = "coupon-apply-complete-topic";
    public static final String COUPON_APPLY_FAILED_TOPIC = "coupon-apply-failed-topic";

    public static final String ORDER_CREATED_TOPIC = "order-created-topic";

    public static final String PRODUCT_TOTAL_PRICE_TOPIC = "product-total_price-topic";
    public static final String PRODUCT_TOTAL_PRICE_FAIL_ROLLBACK = "product-total-price-fail-rollback";

    public static final String USE_USER_POINT_COMPLETE_TOPIC = "use-user-point-complete-topic";
    public static final String USE_USER_POINT_FAILED_TOPIC = "use-user-point-failed-topic";

    public static final String PAYMENT_COMPLETE_TOPIC = "payment-complete-topic";
    public static final String PAYMENT_FAILED_TOPIC = "payment-failed-topic";
}
