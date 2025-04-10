package kr.hhplus.be.server.domain.product.service.model;

import kr.hhplus.be.server.domain.product.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    private Product createProductWithState(int state) {
        return Product.builder()
                .id(1L)
                .name("테스트 상품")
                .price(1000L)
                .state(state)
                .createdAt("2024-04-01 12:00:00")
                .updatedAt("2024-04-01 12:00:00")
                .build();
    }

    @Test
    @DisplayName("상품 상태가 삭제 상태인 경우 isDeleted()는 true를 반환한다")
    void isDeletedTest() {
        Product product = createProductWithState(-1);
        assertThat(product.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("상품 상태가 판매중인 경우 isOnSale()은 true를 반환한다")
    void isOnSaleTest() {
        Product product = createProductWithState(1);
        assertThat(product.isOnSale()).isTrue();
    }

    @Test
    @DisplayName("상품 상태가 품절인 경우 isSoldOut()은 true를 반환한다")
    void isSoldOutTest() {
        Product product = createProductWithState(2);
        assertThat(product.isSoldOut()).isTrue();
    }

    @Test
    @DisplayName("상품 상태가 숨김 상태인 경우 isHidden()은 true를 반환한다")
    void isHiddenTest() {
        Product product = createProductWithState(3);
        assertThat(product.isHidden()).isTrue();
    }

    @Test
    @DisplayName("상품 상태가 null이면 모든 상태 확인 메서드는 false를 반환한다")
    void nullStateTest() {
        Product product = createProductWithState(0);
        assertThat(product.isDeleted()).isFalse();
        assertThat(product.isOnSale()).isFalse();
        assertThat(product.isSoldOut()).isFalse();
        assertThat(product.isHidden()).isFalse();
    }
}

