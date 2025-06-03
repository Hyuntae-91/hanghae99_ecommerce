package kr.hhplus.be.server.common.constants;

public class Groups {

    public static final String COUPON_ISSUE_GROUP = "coupon-issue-consumer";
    public static final String COUPON_APPLY_COMPLETE_GROUP = "coupon-apply-complete-consumer";
    public static final String COUPON_APPLY_FAILED_GROUP = "coupon-apply-failed-consumer";

    public static final String ORDER_CREATED_GROUP = "order-created-consumer";

    public static final String PRODUCT_TOTAL_PRICE_GROUP = "product-total-price-consumer";
    public static final String PRODUCT_TOTAL_PRICE_FAIL_ROLLBACK_GROUP = "product-total-price-fail-rollback-consumer";

    public static final String USE_USER_POINT_COMPLETE_GROUP = "use-user-point-complete-consumer";
    public static final String USE_USER_POINT_FAILED_GROUP = "use-user-point-failed-consumer";

    public static final String PAYMENT_COMPLETE_MOCK_API_GROUP = "payment-complete-mock-api-consumer";
    public static final String PAYMENT_COMPLETE_PRODUCT_SCORES_GROUP = "payment-complete-product-scores-consumer";
    public static final String PAYMENT_COMPLETE_ORDER_UPDATE_GROUP = "payment-complete-order-update-consumer";
    public static final String PAYMENT_FAILED_GROUP = "payment-failed-consumer";
}
