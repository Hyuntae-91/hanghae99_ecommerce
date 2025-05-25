package kr.hhplus.be.server.domain.coupon.event;

import kr.hhplus.be.server.domain.coupon.dto.response.ApplyCouponDiscountServiceResponse;
import kr.hhplus.be.server.domain.coupon.mapper.CouponMapper;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.product.dto.event.ProductTotalPriceCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CouponEventHandler {

    private final CouponService couponService;
    private final ApplicationEventPublisher publisher;
    private final CouponMapper couponMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleApplyCouponRequested(ProductTotalPriceCompletedEvent event) {
        ApplyCouponDiscountServiceResponse response = couponService.applyCouponDiscount(
                couponMapper.toApplyCouponDiscountServiceRequest(event)
        );

        publisher.publishEvent(
                couponMapper.toApplyCouponDiscountCompletedEvent(event, response)
        );
    }
}