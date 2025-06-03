package kr.hhplus.be.server.interfaces.api.order;

import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.domain.product.model.Product;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderControllerTest {

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
        registry.add("test.kafka.topic", () -> "test-consumer-topic");
        registry.add("test.kafka.group", () -> "test-consumer-group");
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
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private OrderOptionJpaRepository orderOptionJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    @DisplayName("성공: 장바구니 추가")
    void add_to_cart_success() throws Exception {
        // given: 필요한 상품 생성
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10000L)
                .build());
        Product product = productJpaRepository.save(Product.builder()
                .name("상품 A")
                .price(1000L)
                .state(1)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(100)
                .stockQuantity(10)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build());

        // when: 장바구니 추가 요청
        String payload = """
        {
          "productId": %d,
          "optionId": %d,
          "quantity": 2
        }
        """.formatted(product.getId(), option.getId());

        mockMvc.perform(post("/v1/order/cart")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartList[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.cartList[0].quantity").value(2))
                .andExpect(jsonPath("$.cartList[0].optionId").value(option.getId()))
                .andExpect(jsonPath("$.totalPrice").value(product.getPrice() * 2));
    }

    @Test
    @Transactional
    @DisplayName("실패: 존재하지 않는 옵션으로 장바구니 추가 시 404")
    void add_to_cart_fail_option_not_found() throws Exception {
        // given: 존재하지 않는 옵션 ID
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        Product product = productJpaRepository.save(Product.builder()
                .name("없는옵션상품").price(1000L).state(1)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10000L)
                .build());

        String payload = """
        {
          "productId": %d,
          "optionId": 9999,
          "quantity": 1
        }
        """.formatted(product.getId());

        // when & then
        mockMvc.perform(post("/v1/order/cart")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("OrderOption not found. id = " + 9999));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저로 장바구니 추가 시 404")
    void add_to_cart_fail_user_not_found() throws Exception {
        // given: userId = 999는 존재하지 않음
        Product product = productJpaRepository.save(Product.builder()
                .name("유저없는상품").price(1000L).state(1)
                .createdAt("2025-04-16T00:00:00").updatedAt("2025-04-16T00:00:00").build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(270)
                .stockQuantity(5)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        String payload = """
        {
          "productId": %d,
          "optionId": %d,
          "quantity": 1
        }
        """.formatted(product.getId(), option.getId());

        // when & then
        mockMvc.perform(post("/v1/order/cart")
                        .header("userId", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("실패: 요청 수량이 재고보다 많을 때 409 Conflict")
    void add_to_cart_fail_stock_exceeded() throws Exception {
        // given: 유효한 유저
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10000L)
                .build());

        // 유효한 상품 및 옵션
        Product product = productJpaRepository.save(Product.builder()
                .name("재고부족상품").price(1000L).state(1)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        OrderOption option = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(product.getId())
                .size(275)
                .stockQuantity(5)
                .createdAt("2025-04-16T00:00:00")
                .updatedAt("2025-04-16T00:00:00")
                .build());

        String payload = """
        {
          "productId": %d,
          "optionId": %d,
          "quantity": 10
        }
        """.formatted(product.getId(), option.getId());

        // when & then
        mockMvc.perform(post("/v1/order/cart")
                        .header("userId", randomUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("재고가 부족합니다. optionId=" + option.getId()));
    }

    @Test
    @DisplayName("성공: 장바구니 다중 항목의 총 금액이 정확히 계산된다")
    void get_cart_total_price_success() throws Exception {
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);

        // 유저 포인트 생성 (필요 시)
        userPointJpaRepository.save(UserPoint.builder()
                .userId(randomUserId)
                .point(10000L)
                .build());

        // 상품 A: 1000원
        Product productA = productJpaRepository.save(Product.builder()
                .name("상품 A")
                .price(1000L)
                .state(1)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build());

        OrderOption optionA = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(productA.getId())
                .size(275)
                .stockQuantity(10)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build());

        // 상품 B: 1500원
        Product productB = productJpaRepository.save(Product.builder()
                .name("상품 B")
                .price(1500L)
                .state(1)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build());

        OrderOption optionB = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(productB.getId())
                .size(280)
                .stockQuantity(10)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build());

        orderItemJpaRepository.save(OrderItem.of(randomUserId, productA.getId(), optionA.getId(), 1000L, 2));
        orderItemJpaRepository.save(OrderItem.of(randomUserId, productB.getId(), optionB.getId(), 1500L, 1));

        // 장바구니 총액 검증
        mockMvc.perform(get("/v1/order/cart")
                        .header("userId", randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(2000 + 1500))  // 3500
                .andExpect(jsonPath("$.cartList.length()").value(2));
    }

}
