package kr.hhplus.be.server.api.controller.product;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.order.repository.OrderItemJpaRepository;
import kr.hhplus.be.server.infrastructure.order.repository.OrderJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductControllerTest {

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
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private OrderOptionRepository orderOptionJpaRepository;

    @Test
    @Transactional
    @DisplayName("성공: 상품 단건 조회")
    void get_product_success() throws Exception {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(1000L)
                .state(1)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build();
        Product saved = productJpaRepository.save(product);

        OrderOption option = OrderOption.builder()
                .productId(saved.getId())
                .size(100)
                .stockQuantity(10)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build();
        orderOptionJpaRepository.save(option);

        // when & then
        mockMvc.perform(get("/v1/products/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("테스트 상품"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.state").value(1))
                .andExpect(jsonPath("$.createdAt").value("2025-04-15T00:00:00"));
    }

    @Test
    @Transactional
    @DisplayName("성공: 상품 리스트 조회")
    void get_product_list_success() throws Exception {
        // given
        productJpaRepository.save(Product.builder()
                .name("상품 A")
                .price(1000L)
                .state(1)
                .createdAt("2025-04-15T00:00:00")
                .updatedAt("2025-04-15T00:00:00")
                .build());

        productJpaRepository.save(Product.builder()
                .name("상품 B")
                .price(2000L)
                .state(1)
                .createdAt("2025-04-14T00:00:00")
                .updatedAt("2025-04-14T00:00:00")
                .build());

        String payload = """
        {
          "page": 1,
          "size": 10,
          "sort": "createdAt"
        }
        """;

        // when & then
        mockMvc.perform(get("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].price").exists())
                .andExpect(jsonPath("$[0].state").value(1));
    }

    @Test
    @Transactional
    @DisplayName("성공: 인기 상품 조회")
    void get_best_products_success() throws Exception {
        // given: 상품 2개
        long randomUserId = ThreadLocalRandom.current().nextInt(1, 100_000);
        Product productA = productJpaRepository.save(Product.builder()
                .name("인기상품 A").price(1000L).state(1)
                .createdAt("2025-04-10T00:00:00").updatedAt("2025-04-10T00:00:00").build());

        Product productB = productJpaRepository.save(Product.builder()
                .name("인기상품 B").price(2000L).state(1)
                .createdAt("2025-04-10T00:00:00").updatedAt("2025-04-10T00:00:00").build());

        // orderOption 생성
        OrderOption optionA = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(productA.getId())
                .size(275)
                .stockQuantity(10)
                .createdAt("2025-04-10T00:00:00")
                .updatedAt("2025-04-10T00:00:00")
                .build());

        OrderOption optionB = orderOptionJpaRepository.save(OrderOption.builder()
                .productId(productB.getId())
                .size(280)
                .stockQuantity(5)
                .createdAt("2025-04-10T00:00:00")
                .updatedAt("2025-04-10T00:00:00")
                .build());

        // 주문 아이템 리스트 생성
        List<OrderItem> items = List.of(
                OrderItem.builder()
                        .userId(randomUserId)
                        .productId(productA.getId())
                        .optionId(optionA.getId())
                        .eachPrice(1000L)
                        .quantity(2)
                        .createdAt("2025-04-10T00:00:00")
                        .updatedAt("2025-04-10T00:00:00")
                        .build(),
                OrderItem.builder()
                        .userId(randomUserId)
                        .productId(productB.getId())
                        .optionId(optionB.getId())
                        .eachPrice(2000L)
                        .quantity(1)
                        .createdAt("2025-04-10T00:00:00")
                        .updatedAt("2025-04-10T00:00:00")
                        .build()
        );

        // 주문 및 아이템
        Order order = orderJpaRepository.save(Order.of(randomUserId, null, 5000L, 1));

        orderItemJpaRepository.save(OrderItem.builder()
                .orderId(order.getId())
                .userId(randomUserId)
                .productId(productA.getId())
                .optionId(optionA.getId())
                .eachPrice(1000L)
                .quantity(2)
                .createdAt("2025-04-10T00:00:00")
                .updatedAt("2025-04-10T00:00:00")
                .build());

        orderItemJpaRepository.save(OrderItem.builder()
                .orderId(order.getId())
                .userId(randomUserId)
                .productId(productB.getId())
                .optionId(optionB.getId())
                .eachPrice(2000L)
                .quantity(1)
                .createdAt("2025-04-10T00:00:00")
                .updatedAt("2025-04-10T00:00:00")
                .build());

        // when & then
        mockMvc.perform(get("/v1/products/bests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(productA.getId())) // 수량 2가 먼저
                .andExpect(jsonPath("$[1].id").value(productB.getId())); // 수량 1이 다음
    }

    @Test
    @DisplayName("성공: 인기 상품 통계 계산 요청")
    void calculate_best_products_success() throws Exception {
        mockMvc.perform(post("/v1/products/best/calculate"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

}
