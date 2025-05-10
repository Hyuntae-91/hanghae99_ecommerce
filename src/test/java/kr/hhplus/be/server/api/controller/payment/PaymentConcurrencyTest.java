package kr.hhplus.be.server.api.controller.payment;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
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
import kr.hhplus.be.server.testhelper.RepositoryCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentConcurrencyTest {

    @Container
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.0")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        if (!mysqlContainer.isRunning()) {
            mysqlContainer.start();
        }
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQL8Dialect");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");

        redisContainer.start();
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

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

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    @BeforeEach
    void cleanup() {
        repositoryCleaner.cleanUpAll();
    }

    private String nowPlus(int days) {
        return java.time.LocalDateTime.now().plusDays(days).toString();
    }

    @Test
    @DisplayName("동시성 테스트: 재고 1개인 상품에 대해 10명이 동시에 결제 시도할 경우 1명만 성공해야 한다")
    void concurrent_payment_exceed_stock() throws Exception {
        // given
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
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

        for (int i = 0; i <= threadCount; i++) {
            long userId = randomUserId + i;
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
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long productPrice = 2000L;

        // 포인트는 1번만 결제 가능하게 설정
        userPointJpaRepository.save(UserPoint.builder().userId(randomUserId).point(2000L).build());

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

        OrderItem item = orderItemJpaRepository.save(OrderItem.of(randomUserId, product.getId(), option.getId(), productPrice, 1));

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
                                    .header("userId", randomUserId)
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
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long productPrice = 2000L;
        int threadCount = 5;

        // 유저 포인트 충분히 지급
        userPointJpaRepository.save(UserPoint.builder().userId(randomUserId).point(10000L).build());

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
        OrderItem item = orderItemJpaRepository.save(OrderItem.of(randomUserId, product.getId(), option.getId(), productPrice, 1));

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
                .userId(randomUserId)
                .couponId(coupon.getId())
                .state(0)
                .startAt(nowPlus(-3))
                .endAt(nowPlus(3))
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
                                    .header("userId", randomUserId)
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
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long productPrice = 2000L;

        // 포인트 생성
        userPointJpaRepository.save(UserPoint.builder().userId(randomUserId).point(10000L).build());

        // 상품 및 옵션
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

        OrderItem item = OrderItem.of(
                randomUserId, product.getId(), option.getId(), productPrice, 1
        );
        item = orderItemJpaRepository.save(item); // 저장 후 itemId 확보

        // 고정 payload (동일 orderItemId 사용)
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
                                    .header("userId", randomUserId)
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

    @Test
    @DisplayName("동시성 테스트: 결제 도중 포인트 충전이 동시에 일어나도 결제가 정확히 1건만 성공한다")
    void concurrent_payment_while_charging_point() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long productPrice = 2000L;

        // 초기 포인트: 2000
        userPointJpaRepository.save(UserPoint.builder().userId(randomUserId).point(2000L).build());

        // 상품 및 옵션 생성
        Product product = productJpaRepository.save(Product.builder()
                .name("충전 중 결제 상품")
                .price(productPrice)
                .state(1)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(270)
                .stockQuantity(1)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        OrderItem item = orderItemJpaRepository.save(OrderItem.of(randomUserId, product.getId(), option.getId(), productPrice, 1));

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

        CountDownLatch latch = new CountDownLatch(2);

        // 결제 쓰레드
        Thread paymentThread = new Thread(() -> {
            try {
                mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)).andReturn();
            } catch (Exception e) {
                System.err.println("결제 중 예외: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        // 충전 쓰레드 (결제 금액보다 충분히 충전)
        Thread chargeThread = new Thread(() -> {
            try {
                Thread.sleep(50); // 결제보다 살짝 늦게 충전 시도
                mockMvc.perform(put("/v1/point")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"point\": 3000}")).andReturn();
            } catch (Exception e) {
                System.err.println("충전 중 예외: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        paymentThread.start();
        chargeThread.start();

        latch.await();

        // then: 결제는 단 1건만 성공해야 한다
        long successCount = paymentRepository.findAll().stream()
                .filter(p -> p.getState() == 1)
                .count();

        assertThat(successCount).isEqualTo(1);

        UserPoint userPoint = userPointJpaRepository.findById(randomUserId).orElseThrow();
        assertThat(userPoint.getPoint()).isEqualTo(3000L);
    }

}
