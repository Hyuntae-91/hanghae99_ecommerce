package kr.hhplus.be.server.domain.payment.service;

import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
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
            // 포인트 검증
            UserPoint userPoint = userPointRepository.get(userId);
            userPoint.validateUsableBalance(totalPrice);

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
