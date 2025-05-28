package kr.hhplus.be.server.interfaces.api.payment;

import com.jayway.jsonpath.JsonPath;
import kr.hhplus.be.server.domain.coupon.mapper.CouponJsonMapper;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.payment.model.Payment;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderItemJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderOptionJpaRepository;
import kr.hhplus.be.server.infrastructure.payment.repository.PaymentJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentControllerTest {

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
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderOptionJpaRepository orderOptionJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    private CouponRedisRepository couponRedisRepository;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    private String nowPlus(int days) {
        return java.time.LocalDateTime.now().plusDays(days).toString();
    }

    @Test
    @DisplayName("성공: 정상 결제 요청 시 결제 완료")
    void pay_success() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        // 유저 포인트 충분 저장
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10_000L)
                .build());

        // 상품 저장
        Product product = productJpaRepository.save(Product.builder()
                .name("결제상품")
                .price(2000L)
                .state(1)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        // 옵션 저장
        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(275)
                .stockQuantity(10)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        // 장바구니 항목 저장
        OrderItem savedItem = orderItemJpaRepository.save(OrderItem.of(
                randomUserId, product.getId(), option.getId(), 2000L, 2
        ));

        // Redis에 옵션 재고 저장
        couponRedisRepository.saveStock(option.getId(), option.getStockQuantity());

        String payload = """
        {
            "products": [
                {
                    "id": %d,
                    "optionId": %d,
                    "itemId": %d,
                    "quantity": 2
                }
            ],
            "couponId": null
        }
        """.formatted(product.getId(), option.getId(), savedItem.getId());

        // when & then
        String response = mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer orderIdInt = JsonPath.read(response, "$.orderId");
        Long orderId = orderIdInt.longValue();
        Order order = orderJpaRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("주문이 저장되지 않았습니다."));

        List<OrderItem> orderItems = orderItemJpaRepository.findAllByOrderId(orderId);

        Payment payment = paymentJpaRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("결제 정보가 존재하지 않습니다."));

        assertThat(order.getState()).isEqualTo(1);
        assertThat(payment.getTotalPrice()).isEqualTo(4000);
        assertThat(orderItems.get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("실패: 주문 항목이 없을 경우 404 응답")
    public void pay_fail_when_order_items_empty() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        // given: 유저 포인트 충분하지만 장바구니는 비어 있음
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10_000L)
                .build());

        // 상품만 존재 (장바구니에는 추가하지 않음)
        Product product = productJpaRepository.save(Product.builder()
                .name("상품").price(2000L).state(1)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(275)
                .stockQuantity(10)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        // when: 장바구니에 추가 없이 결제 요청
        String payload = """
        {
            "products": [
                {
                    "productId": %d,
                    "optionId": %d,
                    "itemId": 9999,
                    "quantity": 2
                }
            ],
            "couponIssueId": null
        }
        """.formatted(product.getId(), option.getId());

        // then: 404 예외 처리
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("주문 항목이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("실패: 요청 수량이 옵션 재고보다 많으면 결제 실패")
    void pay_fail_due_to_stock_lack() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        // given
        userPointJpaRepository.save(UserPoint.builder().userId(randomUserId).point(10000L).build());

        Product product = productJpaRepository.save(Product.builder()
                .name("재고 부족 상품").price(2000L).state(1)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId()).size(275).stockQuantity(1)
                .createdAt("2025-04-16T10:00:00").updatedAt("2025-04-16T10:00:00")
                .build());

        OrderItem item = OrderItem.of(randomUserId, product.getId(), option.getId(), 2000L, 2);
        orderItemJpaRepository.save(item);

        String payload = """
        {
            "products": [
                {
                    "id": %d,
                    "optionId": %d,
                    "itemId": %d,
                    "quantity": 2
                }
            ],
            "couponIssueId": null
        }
        """.formatted(product.getId(), option.getId(), item.getId());

        // when & then
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("재고가 부족합니다. optionId=" + option.getId()));

        // then: 재고 차감되지 않았는지 확인
        OrderOption updated = orderOptionJpaRepository.findById(option.getId()).orElseThrow();
        assertThat(updated.getStockQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패: 보유 포인트가 부족할 경우 결제 실패 (Redis 기반)")
    void pay_fail_due_to_point_lack() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        // 포인트 부족 유저 저장
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(100L)  // 포인트 일부러 적게 설정
                .build());

        // 상품 저장
        Product product = productJpaRepository.save(Product.builder()
                .name("포인트 부족 상품")
                .price(2000L)
                .state(1)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        // 옵션 저장
        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(270)
                .stockQuantity(10)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        // 장바구니 항목 저장
        OrderItem item = orderItemJpaRepository.save(OrderItem.of(
                randomUserId, product.getId(), option.getId(), 2000L, 2
        ));

        // Redis에 상품/옵션 재고 저장
        couponRedisRepository.saveStock(option.getId(), option.getStockQuantity());

        String payload = """
        {
            "products": [
                {
                    "id": %d,
                    "optionId": %d,
                    "itemId": %d,
                    "quantity": 2
                }
            ],
            "couponId": null
        }
        """.formatted(product.getId(), option.getId(), item.getId());

        // when & then
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("포인트가 부족합니다."));
    }

    @Test
    @DisplayName("실패: 쿠폰이 사용 불가 상태일 경우 결제 실패 처리됨")
    void pay_fail_when_coupon_invalid() throws Exception {
        // given
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10000L)
                .build());

        Product product = productJpaRepository.save(Product.builder()
                .name("쿠폰 테스트 상품")
                .price(2000L)
                .state(1)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(275)
                .stockQuantity(10)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build());

        OrderItem item = orderItemJpaRepository.save(OrderItem.of(
                randomUserId, product.getId(), option.getId(), 2000L, 2
        ));

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("test")
                .discount(100)
                .quantity(10)
                .state(1)
                .expirationDays(7)
                .createdAt("2025-04-16T10:00:00")
                .updatedAt("2025-04-16T10:00:00")
                .build()
        );

        CouponIssue invalidCoupon = couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(randomUserId)
                .couponId(coupon.getId())
                .state(1)
                .startAt("2025-04-01T00:00:00")
                .endAt("2025-04-30T23:59:59")
                .createdAt("2025-04-01T00:00:00")
                .updatedAt("2025-04-01T00:00:00")
                .build());

        couponRedisRepository.addCouponForUser(randomUserId, coupon.getId(), 1);

        String payload = """
        {
            "products": [
                {
                    "id": %d,
                    "optionId": %d,
                    "itemId": %d,
                    "quantity": 2
                }
            ],
            "couponId": %d
        }
        """.formatted(product.getId(), option.getId(), item.getId(), invalidCoupon.getId());

        // when & then
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 사용된 쿠폰입니다."));
    }

    @Test
    @DisplayName("성공: FIXED 쿠폰 적용 시 결제 금액에 할인 반영됨")
    void pay_success_with_coupon_discount_applied() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long originalPrice = 5000L;
        int discountAmount = 1000;

        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10_000L)
                .build());

        Product product = productJpaRepository.save(Product.builder()
                .name("쿠폰 상품")
                .price(originalPrice)
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

        OrderItem item = orderItemJpaRepository.save(OrderItem.of(
                randomUserId, product.getId(), option.getId(), originalPrice, 1
        ));

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("테스트 쿠폰")
                .discount(discountAmount)
                .quantity(10)
                .state(1)
                .expirationDays(7)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        couponRedisRepository.saveCouponInfo(coupon.getId(), CouponJsonMapper.toCouponJson(coupon));
        couponRedisRepository.saveStock(coupon.getId(), coupon.getQuantity());
        couponRedisRepository.addCouponForUser(randomUserId, coupon.getId(), 0);

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
          "couponId": %d
        }
        """.formatted(product.getId(), option.getId(), item.getId(), coupon.getId());

        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    @DisplayName("결제 실패 시 모든 상태가 rollback 되어야 한다")
    void rollback_on_payment_failure() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        // 유저 포인트 저장
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10000L)
                .build());

        // 상품 저장
        Product product = productJpaRepository.save(Product.builder()
                .name("롤백 테스트 상품").price(5000L).state(1)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        // 옵션 저장
        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(270)
                .stockQuantity(1)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        // 장바구니 저장
        OrderItem item = orderItemJpaRepository.save(OrderItem.of(
                randomUserId, product.getId(), option.getId(), 5000L, 1
        ));

        // Redis에 옵션 재고 저장
        couponRedisRepository.saveStock(option.getId(), option.getStockQuantity());

        // 쿠폰 저장
        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("롤백 쿠폰")
                .discount(1000)
                .quantity(10)
                .state(1)
                .expirationDays(7)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        // 쿠폰 발급 (이미 사용불가 상태로 설정)
        CouponIssue issue = couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(randomUserId)
                .couponId(coupon.getId())
                .state(1) // 사용 불가 상태
                .startAt("2025-04-01T00:00:00")
                .endAt("2025-04-30T23:59:59")
                .createdAt("2025-04-01T00:00:00")
                .updatedAt("2025-04-01T00:00:00")
                .build());

        // Redis에 쿠폰 정보 저장
        couponRedisRepository.saveCouponInfo(coupon.getId(), CouponJsonMapper.toCouponJson(coupon));
        couponRedisRepository.addCouponForUser(randomUserId, coupon.getId(), 1);

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
          "couponId": %d
        }
        """.formatted(product.getId(), option.getId(), item.getId(), issue.getId());

        // 결제 시도 → 실패해야 함 (쿠폰 사용불가 상태)
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 사용된 쿠폰입니다."));

        // 롤백 검증
        OrderOption afterOption = orderOptionJpaRepository.findById(option.getId()).orElseThrow();
        UserPoint afterPoint = userPointJpaRepository.findById(randomUserId).orElseThrow();

        assertThat(afterOption.getStockQuantity()).isEqualTo(1); // 재고 롤백
        assertThat(afterPoint.getPoint()).isEqualTo(10000L);     // 포인트 롤백
    }

}
