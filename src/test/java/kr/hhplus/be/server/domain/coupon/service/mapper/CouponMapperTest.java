package kr.hhplus.be.server.domain.coupon.service.mapper;

import kr.hhplus.be.server.domain.coupon.dto.request.ApplyCouponDiscountServiceRequest;
import kr.hhplus.be.server.domain.coupon.dto.response.ApplyCouponDiscountServiceResponse;
import kr.hhplus.be.server.domain.coupon.mapper.CouponMapper;
import kr.hhplus.be.server.domain.coupon.mapper.CouponMapperImpl;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponApplyCompletedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductDataIds;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductTotalPriceCompletedPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CouponMapperTest {

    private CouponMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CouponMapperImpl(); // MapStruct 구현체 직접 사용
    }

    @Test
    @DisplayName("성공: ProductTotalPriceCompletedPayload → ApplyCouponDiscountServiceRequest 매핑")
    void map_toApplyCouponDiscountServiceRequest_success() {
        // given
        List<ProductDataIds> items = List.of(
                new ProductDataIds(1L, 101L, 1001L, 1),
                new ProductDataIds(2L, 102L, 1002L, 1)
        );
        ProductTotalPriceCompletedPayload event = new ProductTotalPriceCompletedPayload(
                1L, 10L, 5L, 1L, 10000L, items
        );

        // when
        ApplyCouponDiscountServiceRequest request = mapper.toApplyCouponDiscountServiceRequest(event);

        // then
        assertThat(request.userId()).isEqualTo(10L);
        assertThat(request.couponId()).isEqualTo(5L);
        assertThat(request.originalPrice()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("성공: event + response → ApplyCouponDiscountCompletedPayload 매핑")
    void map_toApplyCouponDiscountCompletedEvent_success() {
        // given
        List<ProductDataIds> items = List.of(
                new ProductDataIds(1L, 101L, 1001L, 1),
                new ProductDataIds(2L, 102L, 1002L, 1)
        );
        ProductTotalPriceCompletedPayload event = new ProductTotalPriceCompletedPayload(
                1L, 20L, 99L, 1L, 8000L, items
        );
        ApplyCouponDiscountServiceResponse response = new ApplyCouponDiscountServiceResponse(7000L);

        // when
        CouponApplyCompletedPayload result = mapper.toCouponApplyCompletedPayload(event, response);

        // then
        assertThat(result.orderId()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(20L);
        assertThat(result.couponId()).isEqualTo(99L);
        assertThat(result.finalPrice()).isEqualTo(7000L);
        assertThat(result.items().isEmpty()).isFalse();
    }
}
