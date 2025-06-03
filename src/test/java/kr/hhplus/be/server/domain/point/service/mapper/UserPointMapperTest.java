package kr.hhplus.be.server.domain.point.service.mapper;

import kr.hhplus.be.server.domain.coupon.dto.event.ApplyCouponDiscountCompletedEvent;
import kr.hhplus.be.server.domain.point.dto.event.PointUsedCompletedEvent;
import kr.hhplus.be.server.domain.point.dto.request.PointUseServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.PointUseServiceResponse;
import kr.hhplus.be.server.domain.point.mapper.UserPointMapper;
import kr.hhplus.be.server.interfaces.event.coupon.payload.CouponApplyCompletedPayload;
import kr.hhplus.be.server.interfaces.event.point.payload.PointUsedCompletedPayload;
import kr.hhplus.be.server.interfaces.event.product.payload.ProductDataIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class UserPointMapperTest {

    private UserPointMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = UserPointMapper.INSTANCE;
    }

    @Test
    @DisplayName("성공: CouponApplyCompletedPayload → PointUseServiceRequest 변환")
    void toPointUseServiceRequest_success() {
        // given
        List<ProductDataIds> items = List.of(
                new ProductDataIds(1L, 101L, 1001L, 1),
                new ProductDataIds(2L, 102L, 1002L, 1)
        );
        CouponApplyCompletedPayload event = new CouponApplyCompletedPayload(
                1L, 10L, 99L, 1L, 5000L, items
        );

        // when
        PointUseServiceRequest request = mapper.toPointUseServiceRequest(event);

        // then
        assertThat(request.userId()).isEqualTo(10L);
        assertThat(request.point()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("실패: finalPrice가 0이면 예외 발생")
    void toPointUseServiceRequest_fail_when_finalPrice_zero() {
        List<ProductDataIds> items = List.of(
                new ProductDataIds(1L, 101L, 1001L, 1),
                new ProductDataIds(2L, 102L, 1002L, 1)
        );
        CouponApplyCompletedPayload event = new CouponApplyCompletedPayload(
                1L, 10L, 99L, 1L,0L, items
        );

        assertThatThrownBy(() -> mapper.toPointUseServiceRequest(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 포인트는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("실패: finalPrice가 음수이면 예외 발생")
    void toPointUseServiceRequest_fail_when_finalPrice_negative() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(
                        1L, 10L, 99L, -100L, List.of(100L)
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finalPrice는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("실패: userId가 1 미만이면 예외 발생")
    void toPointUseServiceRequest_fail_when_userId_invalid() {
        assertThatThrownBy(() ->
                new ApplyCouponDiscountCompletedEvent(
                        1L, 0L, 99L, 1000L, List.of(100L)
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("성공: CouponApplyCompletedPayload + PointUseServiceResponse → PointUsedCompletedEvent 변환")
    void toPointUsedCompletedEvent_success() {
        // given
        List<ProductDataIds> items = List.of(
                new ProductDataIds(1L, 101L, 1001L, 1),
                new ProductDataIds(2L, 102L, 1002L, 1)
        );
        CouponApplyCompletedPayload event = new CouponApplyCompletedPayload(
                2L, 20L, 88L, 1L, 3000L, items
        );

        PointUseServiceResponse response = new PointUseServiceResponse(20L, 3000L);

        // when
        PointUsedCompletedPayload result = mapper.toPointUsedCompletedPayload(event, response);

        // then
        assertThat(result.orderId()).isEqualTo(2L);
        assertThat(result.userId()).isEqualTo(20L);
        assertThat(result.usedPoint()).isEqualTo(3000L);
        assertThat(result.items().isEmpty()).isFalse();
    }
}