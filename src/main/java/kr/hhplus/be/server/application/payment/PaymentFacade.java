package kr.hhplus.be.server.application.payment;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.payment.dto.PaymentFacadeMapperImpl;
import kr.hhplus.be.server.application.payment.dto.PaymentFacadeRequest;
import kr.hhplus.be.server.common.aop.lock.DistributedLock;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.coupon.dto.response.ApplyCouponDiscountServiceResponse;
import kr.hhplus.be.server.domain.order.dto.response.CreateOrderServiceResponse;
import kr.hhplus.be.server.domain.order.dto.request.UpdateOrderServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentOrderItemDto;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.point.dto.request.PointUseServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.PointValidateUsableRequest;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.application.payment.dto.PaymentFacadeMapper;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.payment.service.PaymentService;
import kr.hhplus.be.server.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PointService pointService;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentFacadeMapper paymentMapper = new PaymentFacadeMapperImpl();

    @DistributedLock(key = "'lock:point:user:' + #arg0.userId")
    @Transactional
    public PaymentServiceResponse pay(PaymentFacadeRequest request) {
        // 1. CREATE ORDER
        CreateOrderServiceResponse orderIdDto = orderService.createOrder(paymentMapper.toServiceRequest(request));
        // 2. Calculate price
        ProductTotalPriceResponse totalPrice = productService.calculateTotalPrice(
                paymentMapper.toProductOptionKeyList(request.products())
        );

        // 3. 쿠폰 사용 처리
        ApplyCouponDiscountServiceResponse finalTotalPrice = couponService.applyCouponDiscount(
                paymentMapper.toApplyCouponDiscountServiceRequest(request.couponIssueId(), totalPrice.totalPrice())
        );

        // 4. 계산된 총 가격 order 테이블에 업데이트
        orderService.updateTotalPrice(new UpdateOrderServiceRequest(orderIdDto.orderId(), finalTotalPrice.finalPrice()));

        // 5. UserPoint validation & use
        pointService.validateUsable(new PointValidateUsableRequest(request.userId(), totalPrice.totalPrice()));
        pointService.use(new PointUseServiceRequest(request.userId(), totalPrice.totalPrice()));

        // 6. pay
        List<PaymentOrderItemDto> orderAndOptionIds = request.products().stream()
                .map(p -> new PaymentOrderItemDto(orderIdDto.orderId(), p.itemId(), p.optionId(), p.quantity()))
                .toList();
        PaymentServiceRequest paymentServiceRequest = new PaymentServiceRequest(
                request.userId(), finalTotalPrice.finalPrice(), orderIdDto.orderId()
        );
        PaymentServiceResponse result = paymentService.pay(paymentServiceRequest);

        eventPublisher.publishEvent(new PaymentCompletedEvent(result));
        return result;
    }
}
