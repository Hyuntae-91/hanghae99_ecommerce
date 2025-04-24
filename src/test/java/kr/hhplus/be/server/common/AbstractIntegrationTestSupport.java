package kr.hhplus.be.server.common;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderItemJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderOptionJpaRepository;
import kr.hhplus.be.server.infrastructure.payment.repository.PaymentJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.PointHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTestSupport {

    @Autowired protected PaymentJpaRepository paymentRepository;
    @Autowired protected OrderItemJpaRepository orderItemJpaRepository;
    @Autowired protected OrderOptionJpaRepository orderOptionJpaRepository;
    @Autowired protected OrderJpaRepository orderJpaRepository;
    @Autowired protected ProductJpaRepository productJpaRepository;
    @Autowired protected UserPointJpaRepository userPointJpaRepository;
    @Autowired protected PointHistoryJpaRepository pointHistoryJpaRepository;
    @Autowired protected CouponIssueJpaRepository couponIssueJpaRepository;
    @Autowired protected CouponJpaRepository couponJpaRepository;

    @BeforeEach
    void cleanup() {
        paymentRepository.deleteAll();
        orderItemJpaRepository.deleteAll();
        orderOptionJpaRepository.deleteAll();
        orderJpaRepository.deleteAll();
        productJpaRepository.deleteAll();
        userPointJpaRepository.deleteAll();
        pointHistoryJpaRepository.deleteAll();
        couponIssueJpaRepository.deleteAll();
        couponJpaRepository.deleteAll();
    }
}
