package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.OrderItemRepository;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.dto.*;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        orderService = new OrderService(orderRepository, orderItemRepository);
    }

    @Test
    @DisplayName("성공: 장바구니에 상품 추가")
    void add_cart_success() {
        // given
        AddCartServiceRequest request = new AddCartServiceRequest(
                1L, // userId
                100L, // productId
                10L, // optionId
                1000L, // eachPrice
                2 // quantity
        );

        OrderItem mockItem = OrderItem.of(
                request.userId(),
                request.productId(),
                request.optionId(),
                request.eachPrice(),
                request.quantity()
        );
        mockItem.setOrderOption(OrderOption.builder()
                .id(10L)
                .productId(100L)
                .size(275)
                .stockQuantity(99)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build()
        );

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemRepository.findCartByUserId(1L)).thenReturn(List.of(mockItem));

        // when
        AddCartServiceResponse response = orderService.addCartService(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.cartList()).hasSize(1);
        assertThat(response.cartList().get(0).productId()).isEqualTo(100L);
        assertThat(response.totalPrice()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("성공: 주문 생성")
    void create_order_success() {
        // given
        Long userId = 1L;
        Long couponIssueId = 5L;
        Long totalPrice = 5000L;

        List<CreateOrderItemDto> items = List.of(new CreateOrderItemDto(1L, 1));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(userId, couponIssueId, items);

        Order orderToSave = Order.of(userId, couponIssueId, totalPrice, 0);

        Order savedOrder = Order.builder()
                .id(100L)
                .userId(userId)
                .couponIssueId(couponIssueId)
                .totalPrice(totalPrice)
                .state(0)
                .orderItems(List.of(
                        OrderItem.of(userId, 10L, 1L, 2000L, 1),
                        OrderItem.of(userId, 11L, 2L, 3000L, 1)
                ))
                .createdAt("2025-04-10T12:00:00")
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // when
        var response = orderService.createOrder(request);

        // then
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(anyList());

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("실패: 주문 생성 - orderRepository.save()가 null을 반환")
    void create_order_fail_save_null() {
        // given
        List<CreateOrderItemDto> items = List.of(new CreateOrderItemDto(1L, 1));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 1L, items);

        when(orderRepository.save(any())).thenReturn(null);

        // then
        assertThrows(NullPointerException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("실패: 주문 생성 - orderItemRepository.saveAll()에서 예외 발생")
    void create_order_fail_saveAll_exception() {
        // given
        Long userId = 1L;
        Long couponIssueId = 5L;
        Long totalPrice = 5000L;

        List<CreateOrderItemDto> items = List.of(new CreateOrderItemDto(1L, 1));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(userId, couponIssueId, items);

        Order savedOrder = Order.builder()
                .id(101L)
                .userId(userId)
                .couponIssueId(couponIssueId)
                .totalPrice(totalPrice)
                .state(0)
                .orderItems(List.of(
                        OrderItem.of(userId, 10L, 1L, 2000L, 1),
                        OrderItem.of(userId, 11L, 2L, 3000L, 1)
                ))
                .createdAt("2025-04-10T12:00:00")
                .build();

        when(orderRepository.save(any())).thenReturn(savedOrder);
        doThrow(new RuntimeException("DB Error")).when(orderItemRepository).saveAll(anyList());

        // then
        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("실패: 주문 생성 - 저장된 주문의 orderItems가 null인 경우")
    void create_order_fail_orderItems_null() {
        // given
        List<CreateOrderItemDto> items = List.of(new CreateOrderItemDto(1L, 1));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 1L, items);

        Order saved = Order.builder()
                .id(10L)
                .userId(1L)
                .couponIssueId(1L)
                .totalPrice(3000L)
                .orderItems(null)  // null
                .createdAt("2025-04-10T12:00:00")
                .build();

        when(orderRepository.save(any())).thenReturn(saved);

        // then
        assertThrows(NullPointerException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("실패: 주문 생성 - 주문 항목이 비어있는 경우")
    void create_order_fail_empty_orderItems() {
        // given
        List<CreateOrderItemDto> items = List.of(new CreateOrderItemDto(1L, 1));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 1L, items);

        Order saved = Order.builder()
                .id(10L)
                .userId(1L)
                .couponIssueId(1L)
                .totalPrice(3000L)
                .orderItems(List.of())  // empty
                .createdAt("2025-04-10T12:00:00")
                .build();

        when(orderRepository.save(any())).thenReturn(saved);

        // then
        assertThrows(IllegalStateException.class, () -> orderService.createOrder(request));
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
        item.setOrderOption(OrderOption.builder()
                .id(10L)
                .productId(100L)
                .stockQuantity(99)
                .size(270)
                .createdAt("2025-04-10T12:00:00")
                .updatedAt("2025-04-10T12:00:00")
                .build()
        );

        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of(item));

        // when
        var result = orderService.getCart(new GetCartServiceRequest(userId));

        // then
        assertThat(result).isNotNull();
        assertThat(result.cartList()).hasSize(1);
        assertThat(result.totalPrice()).isEqualTo(1500L);
        assertThat(result.cartList().get(0).productId()).isEqualTo(100L);
        assertThat(result.cartList().get(0).eachPrice()).isEqualTo(1500L);
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
    @DisplayName("실패: findCartByUserId()가 null을 반환하면 NPE 발생")
    void get_cart_fail_when_repository_returns_null() {
        // given
        Long userId = 3L;
        when(orderItemRepository.findCartByUserId(userId)).thenReturn(null);

        // then
        assertThrows(NullPointerException.class, () -> orderService.getCart(new GetCartServiceRequest(userId)));
    }

    @Test
    @DisplayName("실패: OrderItem의 OrderOption이 null일 경우 IllegalStateException")
    void get_cart_fail_order_option_null() {
        // given
        Long userId = 4L;

        OrderItem item = OrderItem.of(userId, 100L, 10L, 2000L, 1);

        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of(item));

        // then
        assertThrows(IllegalStateException.class, () -> orderService.getCart(new GetCartServiceRequest(userId)));
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

}