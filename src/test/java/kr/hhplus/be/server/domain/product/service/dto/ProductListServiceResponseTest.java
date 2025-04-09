package kr.hhplus.be.server.domain.product.service.dto;

import kr.hhplus.be.server.domain.product.dto.ProductListServiceResponse;
import kr.hhplus.be.server.domain.product.dto.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.dto.ProductServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProductListServiceResponse 유효성 검증")
class ProductListServiceResponseTest {

    private static final List<ProductOptionResponse> VALID_OPTIONS = List.of(
            new ProductOptionResponse(1L, 250, 10)
    );

    @Test
    @DisplayName("성공: 유효한 리스트로 생성")
    void validList_doesNotThrow() {
        List<ProductServiceResponse> products = List.of(
                new ProductServiceResponse(1L, "상품", 1000L, 1, "2025-04-01", VALID_OPTIONS)
        );

        assertThatCode(() -> new ProductListServiceResponse(products)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패: 리스트가 null이면 예외 발생")
    void nullList_throwsException() {
        assertThatThrownBy(() -> new ProductListServiceResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("products는 null일 수 없습니다.");
    }
}
