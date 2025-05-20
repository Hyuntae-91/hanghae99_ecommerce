package kr.hhplus.be.server.domain.product.model;

import java.util.List;

public enum ProductStates {
    DELETED(-1),
    AVAILABLE(1),
    SOLD_OUT(2),
    HIDDEN(3);

    private final int code;

    ProductStates(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static boolean isExcluded(int code) {
        return code == DELETED.code || code == SOLD_OUT.code;
    }

    public static List<Integer> excludedInProductList() {
        return List.of(DELETED.code, SOLD_OUT.code);
    }

    public static ProductStates fromCode(int code) {
        for (ProductStates state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStates code: " + code);
    }
}
