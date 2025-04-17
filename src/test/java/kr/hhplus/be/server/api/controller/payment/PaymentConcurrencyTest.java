package kr.hhplus.be.server.api.controller.payment;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderItemJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderOptionJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserPointJpaRepository userPointJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private OrderOptionJpaRepository orderOptionJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Test
    @DisplayName("동시성 테스트: 재고 1개인 상품에 대해 10명이 동시에 결제 시도할 경우 1명만 성공해야 한다")
    void concurrent_payment_exceed_stock() throws Exception {
        // given
        long productPrice = 2000L;
        Long productId, optionId;

        Product product = productJpaRepository.save(Product.builder()
                .name("동시성 상품").price(productPrice).state(1)
                .createdAt("2025-04-16T10:00:00").updatedAt("2025-04-16T10:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId()).size(270).stockQuantity(1)
                .createdAt("2025-04-16T10:00:00").updatedAt("2025-04-16T10:00:00")
                .build());

        productId = product.getId();
        optionId = option.getId();

        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (long userId = 1; userId <= threadCount; userId++) {
            // 포인트 충분히 저장
            userPointJpaRepository.save(UserPoint.builder().userId(userId).point(10_000L).build());

            // 장바구니 저장
            OrderItem item = orderItemJpaRepository.save(OrderItem.of(userId, productId, optionId, productPrice, 1));
            Long itemId = item.getId();

            final String payload = """
            {
              "products": [
                {
                  "id": %d,
                  "optionId": %d,
                  "itemId": %d,
                  "quantity": 1
                }
              ],
              "couponIssueId": null
            }
            """.formatted(productId, optionId, itemId);

            final long uid = userId;

            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/payment")
                                    .header("userId", uid)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload))
                            .andReturn();
                } catch (Exception e) {
                    System.err.println("결제 중 예외: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();

        // then
        long successPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getState() == 1)
                .count();

        assertThat(successPayments).isEqualTo(1);
    }

    @Test
    @DisplayName("동시성 테스트: 단일 유저가 동일 상품에 대해 여러 번 결제 시도 시 포인트 부족으로 1건만 성공")
    void concurrent_payment_single_user_exceed_point() throws Exception {
        // given
        long userId = 1L;
        long productPrice = 2000L;

        // 포인트는 1번만 결제 가능하게 설정
        userPointJpaRepository.save(UserPoint.builder().userId(userId).point(2000L).build());

        Product product = productJpaRepository.save(Product.builder()
                .name("동시성 상품-단일 유저 포인트 초과")
                .price(productPrice)
                .state(1)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(270)
                .stockQuantity(10)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        OrderItem item = orderItemJpaRepository.save(OrderItem.of(userId, product.getId(), option.getId(), productPrice, 1));

        String payload = """
        {
          "products": [
            {
              "id": %d,
              "optionId": %d,
              "itemId": %d,
              "quantity": 1
            }
          ],
          "couponIssueId": null
        }
        """.formatted(product.getId(), option.getId(), item.getId());

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/payment")
                                    .header("userId", userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload))
                            .andReturn();
                } catch (Exception e) {
                    System.err.println("결제 중 예외: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();

        // then
        long successCount = paymentRepository.findAll().stream()
                .filter(p -> p.getState() == 1)
                .count();

        assertThat(successCount).isEqualTo(1);
    }

    @Test
    @DisplayName("동시성 테스트: 하나의 쿠폰에 대해 여러 결제 시도 시 1건만 성공해야 한다")
    void concurrent_payment_with_single_coupon() throws Exception {
        long userId = 1L;
        long productPrice = 2000L;
        int threadCount = 5;

        // 유저 포인트 충분히 지급
        userPointJpaRepository.save(UserPoint.builder().userId(userId).point(10000L).build());

        // 상품 생성
        Product product = productJpaRepository.save(Product.builder()
                .name("동시성 쿠폰 상품").price(productPrice).state(1)
                .createdAt("2025-04-16T00:00:00").updatedAt("2025-04-16T00:00:00")
                .build());

        // 옵션 생성
        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId()).size(270).stockQuantity(10)
                .createdAt("2025-04-16T00:00:00").updatedAt("2025-04-16T00:00:00")
                .build());

        // 장바구니 항목 저장
        OrderItem item = orderItemJpaRepository.save(OrderItem.of(userId, product.getId(), option.getId(), productPrice, 1));

        // 쿠폰 생성
        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("중복방지 쿠폰")
                .discount(1000)
                .quantity(1)
                .issued(1)
                .expirationDays(7)
                .createdAt("2025-04-16T00:00:00").updatedAt("2025-04-16T00:00:00")
                .build());

        CouponIssue issue = couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(userId)
                .couponId(coupon.getId())
                .state(0)
                .startAt("2025-04-01T00:00:00")
                .endAt("2025-04-30T23:59:59")
                .createdAt("2025-04-01T00:00:00")
                .updatedAt("2025-04-01T00:00:00")
                .build());

        String payload = """
        {
          "products": [
            {
              "id": %d,
              "optionId": %d,
              "itemId": %d,
              "quantity": 1
            }
          ],
          "couponIssueId": %d
        }
        """.formatted(product.getId(), option.getId(), item.getId(), issue.getId());

        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/payment")
                                    .header("userId", userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload))
                            .andReturn();
                } catch (Exception e) {
                    System.err.println("결제 중 예외: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();

        long successCount = paymentRepository.findAll().stream()
                .filter(p -> p.getState() == 1)
                .count();

        assertThat(successCount).isEqualTo(1);
    }

    @Test
    @DisplayName("동시성 테스트: 동일 주문 ID로 여러 번 결제를 시도하면 1건만 성공해야 한다")
    void concurrent_payment_same_order_id() throws Exception {
        long userId = 1L;
        long productPrice = 2000L;

        userPointJpaRepository.save(UserPoint.builder().userId(userId).point(10000L).build());

        Product product = productJpaRepository.save(Product.builder()
                .name("중복 결제 방지 상품")
                .price(productPrice)
                .state(1)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(275)
                .stockQuantity(10)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        OrderItem item = orderItemJpaRepository.save(OrderItem.of(
                userId, product.getId(), option.getId(), productPrice, 1
        ));

        Order order = orderJpaRepository.save(Order.of(
                userId, null, productPrice, 0
        ));

        String payload = """
        {
          "products": [
            {
              "id": %d,
              "optionId": %d,
              "itemId": %d,
              "quantity": 1
            }
          ],
          "couponIssueId": null
        }
        """.formatted(product.getId(), option.getId(), item.getId());

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/payment")
                                    .header("userId", userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload))
                            .andReturn();
                } catch (Exception e) {
                    System.err.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        long successCount = paymentRepository.findAll().stream()
                .filter(p -> p.getState() == 1)
                .count();

        assertThat(successCount).isEqualTo(1);
    }


}
