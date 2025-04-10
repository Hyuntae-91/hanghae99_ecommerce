package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.OrderItemRepository;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.dto.AddCartServiceRequest;
import kr.hhplus.be.server.domain.order.dto.AddCartServiceResponse;
import kr.hhplus.be.server.domain.order.dto.CreateOrderServiceRequest;
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
                null,
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

        CreateOrderServiceRequest request = new CreateOrderServiceRequest(userId, couponIssueId, totalPrice);

        Order orderToSave = Order.of(userId, couponIssueId, totalPrice, 0);

        Order savedOrder = Order.builder()
                .id(100L)
                .userId(userId)
                .couponIssueId(couponIssueId)
                .totalPrice(totalPrice)
                .state(0)
                .orderItems(List.of(
                        OrderItem.of(userId, null, 10L, 1L, 2000L, 1),
                        OrderItem.of(userId, null, 11L, 2L, 3000L, 1)
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
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 1L, 3000L);

        when(orderRepository.save(any())).thenReturn(null); // save 결과 null

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

        CreateOrderServiceRequest request = new CreateOrderServiceRequest(userId, couponIssueId, totalPrice);

        Order savedOrder = Order.builder()
                .id(101L)
                .userId(userId)
                .couponIssueId(couponIssueId)
                .totalPrice(totalPrice)
                .state(0)
                .orderItems(List.of(
                        OrderItem.of(userId, null, 10L, 1L, 2000L, 1),
                        OrderItem.of(userId, null, 11L, 2L, 3000L, 1)
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
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 1L, 3000L);

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
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(1L, 1L, 3000L);

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

}