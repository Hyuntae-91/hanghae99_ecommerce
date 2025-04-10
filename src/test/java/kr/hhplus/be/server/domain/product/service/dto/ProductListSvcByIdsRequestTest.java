package kr.hhplus.be.server.domain.product.service.dto;

import kr.hhplus.be.server.domain.product.dto.ProductListSvcByIdsRequest;
import kr.hhplus.be.server.domain.product.dto.ProductOptionKeyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ProductListSvcByIdsRequestTest {

    @Test
    @DisplayName("성공: 유효한 items 리스트로 생성된다")
    void create_success() {
        // given
        ProductOptionKeyDto item = new ProductOptionKeyDto(1L, 1L, 1L);

        // when
        ProductListSvcByIdsRequest request = new ProductListSvcByIdsRequest(List.of(item));

        // then
        assertThat(request.items()).hasSize(1);
        assertThat(request.items().get(0).productId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패: items가 null일 경우 예외 발생")
    void create_fail_when_null() {
        // when & then
        assertThatThrownBy(() -> new ProductListSvcByIdsRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 목록은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: items가 빈 리스트일 경우 예외 발생")
    void create_fail_when_empty() {
        // when & then
        assertThatThrownBy(() -> new ProductListSvcByIdsRequest(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 목록은 비어 있을 수 없습니다.");
    }
}
