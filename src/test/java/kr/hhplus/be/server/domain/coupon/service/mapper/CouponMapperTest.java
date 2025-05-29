package kr.hhplus.be.server.domain.coupon.service.mapper;

import kr.hhplus.be.server.domain.coupon.dto.event.ApplyCouponDiscountCompletedEvent;
import kr.hhplus.be.server.domain.coupon.dto.request.ApplyCouponDiscountServiceRequest;
import kr.hhplus.be.server.domain.coupon.dto.response.ApplyCouponDiscountServiceResponse;
import kr.hhplus.be.server.domain.coupon.mapper.CouponMapper;
import kr.hhplus.be.server.domain.coupon.mapper.CouponMapperImpl;
import kr.hhplus.be.server.domain.product.dto.event.ProductTotalPriceCompletedEvent;
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
    @DisplayName("성공: ProductTotalPriceCompletedEvent → ApplyCouponDiscountServiceRequest 매핑")
    void map_toApplyCouponDiscountServiceRequest_success() {
        // given
        ProductTotalPriceCompletedEvent event = new ProductTotalPriceCompletedEvent(
                1L, 10L, 5L, 1L, 10000L, List.of(1L, 2L)
        );

        // when
        ApplyCouponDiscountServiceRequest request = mapper.toApplyCouponDiscountServiceRequest(event);

        // then
        assertThat(request.userId()).isEqualTo(10L);
        assertThat(request.couponId()).isEqualTo(5L);
        assertThat(request.originalPrice()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("성공: event + response → ApplyCouponDiscountCompletedEvent 매핑")
    void map_toApplyCouponDiscountCompletedEvent_success() {
        // given
        ProductTotalPriceCompletedEvent event = new ProductTotalPriceCompletedEvent(
                1L, 20L, 99L, 1L, 8000L, List.of(10L, 20L)
        );
        ApplyCouponDiscountServiceResponse response = new ApplyCouponDiscountServiceResponse(7000L);

        // when
        ApplyCouponDiscountCompletedEvent result = mapper.toApplyCouponDiscountCompletedEvent(event, response);

        // then
        assertThat(result.orderId()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(20L);
        assertThat(result.couponId()).isEqualTo(99L);
        assertThat(result.finalPrice()).isEqualTo(7000L);
        assertThat(result.productIds()).containsExactly(10L, 20L);
    }
}
