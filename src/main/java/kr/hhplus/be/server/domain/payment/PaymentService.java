package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.coupon.CouponIssueRepository;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.order.OrderItemRepository;
import kr.hhplus.be.server.domain.order.OrderOptionRepository;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.payment.dto.PaymentOrderItemDto;
import kr.hhplus.be.server.domain.payment.dto.PaymentServiceRequest;
import kr.hhplus.be.server.domain.payment.dto.PaymentServiceResponse;
import kr.hhplus.be.server.domain.payment.model.Payment;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.point.model.PointHistoryType;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.infrastructure.point.dto.GetPointRepositoryRequestDto;
import kr.hhplus.be.server.infrastructure.point.dto.SavePointHistoryRepoRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderItemRepository orderItemRepository;
    private final OrderOptionRepository orderOptionRepository;
    private final PaymentRepository paymentRepository;
    private final PointRepository userPointRepository;
    private final CouponIssueRepository couponIssueRepository;

    private PaymentServiceResponse toResponse(Payment payment) {
        return new PaymentServiceResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getTotalPrice(),
                payment.getCreatedAt()
        );
    }

    public PaymentServiceResponse pay(PaymentServiceRequest request) {
        Long userId = request.userId();
        long totalPrice = request.totalPrice();
        List<OrderItem> orderItems = null;
        Long orderId = null;

        try {
            List<Long> orderItemIds = request.orderItems().stream()
                    .map(PaymentOrderItemDto::orderItemId)
                    .toList();

            orderItems = orderItemRepository.findByIds(orderItemIds);
            orderId = orderItems.get(0).getOrderId();

            for (OrderItem item : orderItems) {
                OrderOption option = orderOptionRepository.getById(item.getOptionId());
                option.validateEnoughStock(item.getQuantity());
            }

            UserPoint userPoint = userPointRepository.get(new GetPointRepositoryRequestDto(userId));
            userPoint.validateUsableBalance(totalPrice);

            for (OrderItem item : orderItems) {
                OrderOption option = orderOptionRepository.getById(item.getOptionId());
                option.decreaseStock(item.getQuantity());
                orderOptionRepository.save(option);  // TODO: 병목지점 될 수 있음. 개선 필요
            }

            if (request.couponIssueId() != null && request.couponIssueId() > 0) {
                CouponIssue issue = couponIssueRepository.findById(request.couponIssueId());
                issue.markUsed();
                couponIssueRepository.save(issue);
            }

            userPoint.use(totalPrice);
            userPointRepository.savePoint(userPoint);
            SavePointHistoryRepoRequestDto historyDto = new SavePointHistoryRepoRequestDto(
                    userId,
                    totalPrice,
                    PointHistoryType.USE
            );
            userPointRepository.saveHistory(historyDto);

            Payment payment = Payment.of(
                    orderItems.get(0).getOrderId(),
                    1,
                    totalPrice
            );
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
