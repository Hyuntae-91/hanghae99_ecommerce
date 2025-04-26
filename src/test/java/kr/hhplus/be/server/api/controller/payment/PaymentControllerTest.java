package kr.hhplus.be.server.api.controller.payment;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponIssue;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.domain.coupon.repository.CouponIssueRepository;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderItemJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderOptionJpaRepository;
import kr.hhplus.be.server.infrastructure.point.repository.UserPointJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private CouponIssueJpaRepository couponIssueJpaRepository;

    @Test
    @Transactional
    @DisplayName("성공: 정상 결제 요청 시 결제 완료")
    public void pay_success() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        // given: 유저 포인트 충분
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10_000L)
                .build());

        // 상품 + 옵션 저장
        Product product = productJpaRepository.save(Product.builder()
                .name("결제상품")
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

        OrderItem item = OrderItem.of(randomUserId, product.getId(), option.getId(), 2000L, 2);
        OrderItem savedItem = orderItemJpaRepository.save(item);

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
        """.formatted(product.getId(), option.getId(), savedItem.getId());

        // when & then: 결제 성공 응답
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.total_price").value(4000))
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.paymentId").exists());
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
    @DisplayName("실패: 보유 포인트가 부족할 경우 결제 실패")
    void pay_fail_due_to_point_lack() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        userPointJpaRepository.save(UserPoint.builder().userId(randomUserId).point(100L).build());

        Product product = productJpaRepository.save(Product.builder()
                .name("포인트 부족 상품").price(2000L).state(1)
                .createdAt("2025-04-16T10:00:00").updatedAt("2025-04-16T10:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId()).size(270).stockQuantity(10)
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

        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(-1));
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
                .name("쿠폰 테스트 상품").price(2000L).state(1)
                .createdAt("2025-04-16T10:00:00").updatedAt("2025-04-16T10:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId()).size(275).stockQuantity(10)
                .createdAt("2025-04-16T10:00:00").updatedAt("2025-04-16T10:00:00")
                .build());

        OrderItem item = OrderItem.of(randomUserId, product.getId(), option.getId(), 2000L, 2);
        orderItemJpaRepository.save(item);

        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .id(1L)
                .type(CouponType.FIXED)
                .description("test")
                .discount(100)
                .quantity(10)
                .issued(10)
                .expirationDays(7)
                .createdAt("2025-04-16T10:00:00").updatedAt("2025-04-16T10:00:00")
                .build()
        );

        CouponIssue invalidCoupon = couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(randomUserId)
                .couponId(1L)
                .state(1)
                .startAt("2025-04-01T00:00:00")
                .endAt("2025-04-30T23:59:59")
                .createdAt("2025-04-01T00:00:00")
                .updatedAt("2025-04-01T00:00:00")
                .build());
        Long couponIssueId = invalidCoupon.getId();

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
            "couponIssueId": %d
        }
        """.formatted(product.getId(), option.getId(), item.getId(), couponIssueId);

        // when & then
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사용할 수 없는 쿠폰입니다."));

    }

    @Test
    @DisplayName("성공: FIXED 쿠폰 적용 시 결제 금액에 할인 반영됨")
    void pay_success_with_coupon_discount_applied() throws Exception {
        // given
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long originalPrice = 5000L;
        int discountAmount = 1000;

        // 1. 유저 포인트 충전
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10_000L)
                .build());

        // 2. 상품 & 옵션
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

        // 3. 장바구니 항목 저장
        OrderItem item = orderItemJpaRepository.save(OrderItem.of(
                randomUserId,
                product.getId(),
                option.getId(),
                originalPrice,
                1
        ));

        // 4. 쿠폰 발급 (FIXED: 1000원 할인)
        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("테스트 쿠폰")
                .discount(discountAmount)
                .quantity(10)
                .issued(1)
                .expirationDays(7)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        CouponIssue issue = couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(randomUserId)
                .couponId(coupon.getId())
                .state(0)
                .startAt("2025-04-01T00:00:00")
                .endAt("2025-04-30T23:59:59")
                .createdAt("2025-04-01T00:00:00")
                .updatedAt("2025-04-01T00:00:00")
                .build());

        // 5. Payload 구성
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

        // when & then
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.total_price").value(originalPrice - discountAmount));
    }

    @Test
    @DisplayName("결제 실패 시 모든 상태가 rollback 되어야 한다")
    void rollback_on_payment_failure() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        long productPrice = 5000L;
        int discount = 1000;

        // 유저 포인트 세팅
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10000L)
                .build());

        // 상품 및 옵션 생성
        Product product = productJpaRepository.save(Product.builder()
                .name("롤백 테스트 상품").price(productPrice).state(1)
                .createdAt("2025-04-16T00:00:00").updatedAt("2025-04-16T00:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(270)
                .stockQuantity(1)
                .createdAt("2025-04-16T00:00:00").updatedAt("2025-04-16T00:00:00")
                .build());

        // 장바구니 아이템
        OrderItem item = orderItemJpaRepository.save(OrderItem.of(
                randomUserId, product.getId(), option.getId(), productPrice, 1
        ));

        // 쿠폰 발급
        Coupon coupon = couponJpaRepository.save(Coupon.builder()
                .type(CouponType.FIXED)
                .description("롤백 쿠폰")
                .discount(discount)
                .quantity(10)
                .issued(1)
                .expirationDays(7)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        CouponIssue issue = couponIssueJpaRepository.save(CouponIssue.builder()
                .userId(randomUserId)
                .couponId(coupon.getId())
                .state(0)
                .startAt("2025-04-01T00:00:00")
                .endAt("2025-04-30T23:59:59")
                .createdAt("2025-04-01T00:00:00")
                .updatedAt("2025-04-01T00:00:00")
                .build());

        // 쿠폰을 사용불가로 만들기 → 결제 실패 유도
        issue.updateState(1);
        couponIssueJpaRepository.save(issue);

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

        // when & then
        mockMvc.perform(post("/v1/payment")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사용할 수 없는 쿠폰입니다."));

        // 검증
        OrderOption afterOption = orderOptionJpaRepository.findById(option.getId()).get();
        UserPoint afterPoint = userPointJpaRepository.findById(randomUserId).get();
        CouponIssue afterIssue = couponIssueJpaRepository.findById(issue.getId()).get();

        assertThat(afterOption.getStockQuantity()).isEqualTo(1);
        assertThat(afterPoint.getPoint()).isEqualTo(10000L);
        assertThat(afterIssue.getState()).isEqualTo(1);  // 이미 사용불가였으므로 유지됨
    }

}
