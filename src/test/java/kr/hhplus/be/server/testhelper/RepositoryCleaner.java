package kr.hhplus.be.server.testhelper;

import kr.hhplus.be.server.infrastructure.coupon.repository.*;
import kr.hhplus.be.server.infrastructure.order.repository.*;
import kr.hhplus.be.server.infrastructure.payment.repository.*;
import kr.hhplus.be.server.infrastructure.point.repository.*;
import kr.hhplus.be.server.infrastructure.product.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepositoryCleaner {

    @Autowired private CouponIssueJpaRepository couponIssueRepo;
    @Autowired private CouponJpaRepository couponRepo;
    @Autowired private OrderItemJpaRepository orderItemRepo;
    @Autowired private OrderJpaRepository orderRepo;
    @Autowired private OrderOptionJpaRepository orderOptionRepo;
    @Autowired private PaymentJpaRepository paymentRepo;
    @Autowired private PointHistoryJpaRepository pointHistoryRepo;
    @Autowired private UserPointJpaRepository userPointRepo;
    @Autowired private ProductJpaRepository productRepo;

    public void cleanUpAll() {
        // 순서 중요: 외래 키 제약이 있다면 의존도 낮은 테이블부터 삭제
        paymentRepo.deleteAll();
        orderItemRepo.deleteAll();
        orderRepo.deleteAll();
        orderOptionRepo.deleteAll();
        couponIssueRepo.deleteAll();
        couponRepo.deleteAll();
        pointHistoryRepo.deleteAll();
        userPointRepo.deleteAll();
        productRepo.deleteAll();
    }
}
