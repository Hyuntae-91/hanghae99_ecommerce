package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.dto.request.*;
import kr.hhplus.be.server.domain.order.dto.response.AddCartServiceResponse;
import kr.hhplus.be.server.domain.order.dto.response.CartItemResponse;
import kr.hhplus.be.server.domain.order.dto.response.CartItemServiceResponse;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.exception.custom.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private OrderOptionRepository orderOptionRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        orderOptionRepository = mock(OrderOptionRepository.class);
        orderService = new OrderService(orderRepository, orderItemRepository, orderOptionRepository);
    }

    @Test
    @DisplayName("성공: 장바구니에 상품 추가")
    void add_cart_success() {
        // given
        AddCartServiceRequest request = new AddCartServiceRequest(
                1L, 100L, 10L, 1000L, 2
        );

        OrderItem mockItem = OrderItem.of(
                request.userId(),
                request.productId(),
                request.optionId(),
                request.eachPrice(),
                request.quantity()
        );

        OrderOption option = OrderOption.builder()
                .id(10L)
                .productId(100L)
                .size(275)
                .stockQuantity(99)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemRepository.findCartByUserId(1L)).thenReturn(List.of(mockItem));
        when(orderOptionRepository.findWithLockById(10L)).thenReturn(option);

        // when
        AddCartServiceResponse response = orderService.addCartService(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.cartList()).hasSize(1);
        assertThat(response.cartList().get(0).productId()).isEqualTo(100L);
        assertThat(response.totalPrice()).isEqualTo(1000L * 2);
    }

    @Test
    @DisplayName("실패: 주문 생성 - orderItemRepository.saveAll()에서 예외 발생")
    void create_order_fail_saveAll_exception() {
        // given
        Long userId = 1L;
        Long couponIssueId = 5L;

        List<CreateOrderOptionDto> items = List.of(new CreateOrderOptionDto(1L, 1));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(userId, couponIssueId, items);

        Order savedOrder = Order.builder()
                .id(101L)
                .userId(userId)
                .couponIssueId(couponIssueId)
                .totalPrice(0L)
                .state(0)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        when(orderRepository.save(any())).thenReturn(savedOrder);
        doThrow(new RuntimeException("DB Error")).when(orderItemRepository).saveAll(anyList());

        // then
        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("성공: 장바구니 조회")
    void get_cart_success() {
        // given
        Long userId = 1L;

        OrderItem item = OrderItem.of(
                userId,
                100L,
                10L,
                1500L,
                2
        );

        OrderOption option = OrderOption.builder()
                .id(10L)
                .productId(100L)
                .stockQuantity(99)
                .size(270)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build();

        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of(item));
        when(orderOptionRepository.findWithLockById(10L)).thenReturn(option);

        // when
        var result = orderService.getCart(new GetCartServiceRequest(userId));

        // then
        assertThat(result).isNotNull();
        assertThat(result.cartList()).hasSize(1);
        assertThat(result.totalPrice()).isEqualTo(1500L * 2);
        assertThat(result.cartList().get(0).productId()).isEqualTo(100L);
        assertThat(result.cartList().get(0).eachPrice()).isEqualTo(1500L);
        assertThat(result.cartList().get(0).stockQuantity()).isEqualTo(99);
        assertThat(result.cartList().get(0).size()).isEqualTo(270);
    }

    @Test
    @DisplayName("실패: 장바구니가 비어있을 경우 예외 발생")
    void get_cart_fail_when_empty() {
        // given
        Long userId = 2L;
        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of());

        // expect
        assertThrows(IllegalArgumentException.class, () -> orderService.getCart(new GetCartServiceRequest(userId)));
    }

    @Test
    @DisplayName("실패: OrderOption이 존재하지 않으면 예외 발생")
    void get_cart_fail_order_option_not_found() {
        // given
        Long userId = 4L;

        OrderItem item = OrderItem.of(userId, 100L, 10L, 2000L, 1);

        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of(item));
        when(orderOptionRepository.findWithLockById(10L))
                .thenThrow(new ResourceNotFoundException("OrderOption not found"));

        // then
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getCart(new GetCartServiceRequest(userId)));
    }

    @Test
    @DisplayName("성공: 주문 총액 업데이트")
    void update_total_price_success() {
        // given
        Long orderId = 1L;
        long newTotalPrice = 9999L;

        Order order = mock(Order.class);
        when(orderRepository.getById(orderId)).thenReturn(order);

        UpdateOrderServiceRequest request = new UpdateOrderServiceRequest(orderId, newTotalPrice);

        // when
        orderService.updateTotalPrice(request);

        // then
        verify(orderRepository, times(1)).getById(orderId);
        verify(order, times(1)).applyTotalPrice(newTotalPrice);
    }

    @Test
    @DisplayName("성공: 장바구니에 여러 상품이 추가될 경우 총 금액과 항목이 정확히 계산된다")
    void add_multiple_items_to_cart_success() {
        // given
        long userId = 1L;

        // OrderItem A
        OrderItem itemA = OrderItem.of(userId, 100L, 1001L, 1000L, 2);
        OrderOption optionA = OrderOption.builder()
                .id(1001L)
                .productId(100L)
                .size(275)
                .stockQuantity(10)
                .build();

        // OrderItem B
        OrderItem itemB = OrderItem.of(userId, 200L, 2001L, 1500L, 1);
        OrderOption optionB = OrderOption.builder()
                .id(2001L)
                .productId(200L)
                .size(280)
                .stockQuantity(5)
                .build();

        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of(itemA, itemB));
        when(orderOptionRepository.findWithLockById(1001L)).thenReturn(optionA);
        when(orderOptionRepository.findWithLockById(2001L)).thenReturn(optionB);

        // when
        CartItemServiceResponse result = orderService.getCart(new GetCartServiceRequest(userId));

        // then
        assertThat(result.cartList()).hasSize(2);
        assertThat(result.totalPrice()).isEqualTo(2000 + 1500);

        CartItemResponse itemResA = result.cartList().get(0);
        assertThat(itemResA.productId()).isEqualTo(100L);
        assertThat(itemResA.optionId()).isEqualTo(1001L);
        assertThat(itemResA.size()).isEqualTo(275);
        assertThat(itemResA.stockQuantity()).isEqualTo(10);
        assertThat(itemResA.eachPrice()).isEqualTo(1000L);
        assertThat(itemResA.quantity()).isEqualTo(2);

        CartItemResponse itemResB = result.cartList().get(1);
        assertThat(itemResB.productId()).isEqualTo(200L);
        assertThat(itemResB.optionId()).isEqualTo(2001L);
        assertThat(itemResB.size()).isEqualTo(280);
        assertThat(itemResB.stockQuantity()).isEqualTo(5);
        assertThat(itemResB.eachPrice()).isEqualTo(1500L);
        assertThat(itemResB.quantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 주문 ID로 총액 업데이트 시 예외 발생")
    void updateTotalPrice_fail_when_order_not_found() {
        Long orderId = 999L;

        when(orderRepository.getById(orderId))
                .thenThrow(new ResourceNotFoundException("Order Not Found"));

        assertThatThrownBy(() -> orderService.updateTotalPrice(new UpdateOrderServiceRequest(orderId, 0L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order Not Found");
    }

}