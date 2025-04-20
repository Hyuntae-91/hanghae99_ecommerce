package kr.hhplus.be.server.domain.payment.service;

import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentOrderItemDto;
import kr.hhplus.be.server.domain.payment.dto.request.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.response.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.model.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderItemRepository orderItemRepository;
    private final OrderOptionRepository orderOptionRepository;
    private final PaymentRepository paymentRepository;
    private final PointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final CouponIssueRepository couponIssueRepository;

    private PaymentServiceResponse toResponse(Payment payment) {
        return new PaymentServiceResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getState(),
                payment.getTotalPrice(),
                payment.getCreatedAt()
        );
    }

    public PaymentServiceResponse pay(PaymentServiceRequest request) {
        Long userId = request.userId();
        long totalPrice = request.totalPrice();
        Long orderId = request.orderItems().get(0).orderId();

        try {
            // 주문 항목 조회
            List<Long> orderItemIds = request.orderItems().stream()
                    .map(PaymentOrderItemDto::orderItemId)
                    .toList();
            List<OrderItem> orderItems = orderItemRepository.findByIds(orderItemIds);

            // optionId → OrderOption 매핑
            var optionMap = orderItems.stream()
                    .map(OrderItem::getOptionId)
                    .distinct()
                    .collect(Collectors.toMap(
                            id -> id,
                            orderOptionRepository::getById
                    ));

            // 재고 확인
            for (OrderItem item : orderItems) {
                OrderOption option = optionMap.get(item.getOptionId());
                option.validateEnoughStock(item.getQuantity());
            }

            // 포인트 검증
            UserPoint userPoint = userPointRepository.get(userId);
            userPoint.validateUsableBalance(totalPrice);

            // 재고 차감
            for (OrderItem item : orderItems) {
                OrderOption option = optionMap.get(item.getOptionId());
                option.decreaseStock(item.getQuantity());
                orderOptionRepository.save(option);  // TODO: 병목지점 될 수 있음. 개선 필요
            }

            // 쿠폰 사용 처리
            if (request.couponIssueId() != null && request.couponIssueId() > 0) {
                CouponIssue issue = couponIssueRepository.findById(request.couponIssueId());
                issue.markUsed();
                couponIssueRepository.save(issue);
            }

            // 포인트 차감 및 이력 저장
            userPoint.use(totalPrice);
            userPointRepository.savePoint(userPoint);
            pointHistoryRepository.saveHistory(userId, totalPrice, PointHistoryType.USE);

            // 결제 처리
            Payment payment = Payment.of(orderId, 1, totalPrice);
            Payment savedPayment = paymentRepository.save(payment);
            return toResponse(savedPayment);
        } catch (Exception e) {
            // 결제 실패 처리: status -1
            Payment failedPayment = Payment.of(orderId, -1, totalPrice);
            Payment savedFailed = paymentRepository.save(failedPayment);
            return toResponse(savedFailed);
        }
    }
}
