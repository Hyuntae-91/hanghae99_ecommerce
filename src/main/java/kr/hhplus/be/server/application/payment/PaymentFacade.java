package kr.hhplus.be.server.application.payment;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.payment.dto.PaymentFacadeMapperImpl;
import kr.hhplus.be.server.application.payment.dto.PaymentFacadeRequest;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.coupon.dto.ApplyCouponDiscountServiceRequest;
import kr.hhplus.be.server.domain.coupon.dto.ApplyCouponDiscountServiceResponse;
import kr.hhplus.be.server.domain.order.dto.CreateOrderItemDto;
import kr.hhplus.be.server.domain.order.dto.CreateOrderServiceRequest;
import kr.hhplus.be.server.domain.order.dto.CreateOrderServiceResponse;
import kr.hhplus.be.server.domain.order.dto.UpdateOrderServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.PaymentOrderItemDto;
import kr.hhplus.be.server.domain.payment.dto.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
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

    @Transactional
    public PaymentServiceResponse pay(PaymentFacadeRequest request) {
        UserPointServiceRequest userPointServiceRequest = new UserPointServiceRequest(request.userId());
        UserPointServiceResponse userPoint = pointService.getUserPoint(userPointServiceRequest);

        List<CreateOrderItemDto> orderItems = request.products().stream()
                .map(p -> new CreateOrderItemDto(p.itemId(), p.quantity()))
                .toList();
        CreateOrderServiceRequest createOrderRequest = new CreateOrderServiceRequest(
                request.userId(), request.couponIssueId(), orderItems
        );
        CreateOrderServiceResponse orderIdDto = orderService.createOrder(createOrderRequest);

        ProductTotalPriceResponse totalPrice = productService.calculateTotalPrice(
                paymentMapper.toProductOptionKeyList(request.products())
        );

        long finalTotalPrice = totalPrice.totalPrice();
        if (request.couponIssueId() != null && request.couponIssueId() > 0) {
            ApplyCouponDiscountServiceResponse applyCouponPrice = couponService.applyCouponDiscount(
                    new ApplyCouponDiscountServiceRequest(request.couponIssueId(), finalTotalPrice)
            );
            finalTotalPrice = applyCouponPrice.finalPrice();
        }

        orderService.updateTotalPrice(new UpdateOrderServiceRequest(orderIdDto.orderId(), finalTotalPrice));

        List<PaymentOrderItemDto> orderAndOptionIds = request.products().stream()
                .map(p -> new PaymentOrderItemDto(p.itemId(), p.optionId()))
                .toList();
        PaymentServiceRequest paymentServiceRequest = new PaymentServiceRequest(
                request.userId(), totalPrice.totalPrice(), request.couponIssueId(), orderAndOptionIds
        );
        PaymentServiceResponse result = paymentService.pay(paymentServiceRequest);

        eventPublisher.publishEvent(new PaymentCompletedEvent(result));
        return result;
    }
}
