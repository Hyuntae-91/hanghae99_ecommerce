package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.dto.request.*;
import kr.hhplus.be.server.domain.order.dto.response.AddCartServiceResponse;
import kr.hhplus.be.server.domain.order.dto.response.CartItemResponse;
import kr.hhplus.be.server.domain.order.dto.response.CartItemServiceResponse;
import kr.hhplus.be.server.domain.order.mapper.OrderMapper;
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
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private OrderOptionRepository orderOptionRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private OrderMapper orderMapper;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        orderOptionRepository = mock(OrderOptionRepository.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        orderMapper = mock(OrderMapper.class);
        orderService = new OrderService(orderRepository, orderItemRepository, orderOptionRepository, applicationEventPublisher, orderMapper);
    }

    @Test
    @DisplayName("성공: 장바구니에 상품 추가")
    void add_cart_success() {
        // given
        AddCartServiceRequest request = new AddCartServiceRequest(1L, 100L, 10L, 1000L, 2);
        OrderItem mockItem = OrderItem.of(request.userId(), request.productId(), request.optionId(), request.eachPrice(), request.quantity());
        OrderOption option = OrderOption.builder().id(10L).productId(100L).size(275).stockQuantity(99).createdAt("2025-04-10T12:00:00").updatedAt("2025-04-10T12:00:00").build();

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockItem);
        when(orderItemRepository.findCartByUserId(1L)).thenReturn(List.of(mockItem));
        when(orderOptionRepository.findWithLockById(10L)).thenReturn(option);

        // when
        AddCartServiceResponse response = orderService.addCartService(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.cartList()).hasSize(1);
        assertThat(response.cartList().get(0).productId()).isEqualTo(100L);
        assertThat(response.totalPrice()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("실패: 주문 생성 중 orderItemRepository.saveAll()에서 예외 발생")
    void create_order_fail_saveAll_exception() {
        // given
        Long userId = 1L;
        Long couponIssueId = 5L;
        List<CreateOrderOptionDto> items = List.of(new CreateOrderOptionDto(1L, 1));
        CreateOrderServiceRequest request = new CreateOrderServiceRequest(userId, couponIssueId, items);
        Order savedOrder = Order.builder().id(101L).userId(userId).couponIssueId(couponIssueId).totalPrice(0L).state(0).createdAt("2025-04-10T12:00:00").updatedAt("2025-04-10T12:00:00").build();

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
        OrderItem item = OrderItem.of(userId, 100L, 10L, 1500L, 2);
        OrderOption option = OrderOption.builder().id(10L).productId(100L).stockQuantity(99).size(270).createdAt("2025-04-10T12:00:00").updatedAt("2025-04-10T12:00:00").build();

        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of(item));
        when(orderOptionRepository.findWithLockById(10L)).thenReturn(option);

        // when
        var result = orderService.getCart(new GetCartServiceRequest(userId));

        // then
        assertThat(result).isNotNull();
        assertThat(result.cartList()).hasSize(1);
        assertThat(result.totalPrice()).isEqualTo(3000L);
    }

    @Test
    @DisplayName("실패: 장바구니가 비어있는 경우 예외 발생")
    void get_cart_fail_when_empty() {
        Long userId = 2L;
        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> orderService.getCart(new GetCartServiceRequest(userId)));
    }

    @Test
    @DisplayName("실패: OrderOption 조회 실패 시 예외 발생")
    void get_cart_fail_order_option_not_found() {
        Long userId = 4L;
        OrderItem item = OrderItem.of(userId, 100L, 10L, 2000L, 1);
        when(orderItemRepository.findCartByUserId(userId)).thenReturn(List.of(item));
        when(orderOptionRepository.findWithLockById(10L)).thenThrow(new ResourceNotFoundException("OrderOption not found"));
        assertThrows(ResourceNotFoundException.class, () -> orderService.getCart(new GetCartServiceRequest(userId)));
    }

    @Test
    @DisplayName("성공: 주문 총액 업데이트")
    void update_total_price_success() {
        Long orderId = 1L;
        long newTotalPrice = 9999L;
        Order order = mock(Order.class);
        when(orderRepository.getById(orderId)).thenReturn(order);
        UpdateOrderServiceRequest request = new UpdateOrderServiceRequest(orderId, newTotalPrice);

        // when
        orderService.updateOrder(request);

        // then
        verify(order).applyTotalPrice(newTotalPrice);
        verify(order).updateState(1);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 주문 ID로 총액 업데이트 시 예외 발생")
    void updateTotalPrice_fail_when_order_not_found() {
        Long orderId = 999L;
        when(orderRepository.getById(orderId)).thenThrow(new ResourceNotFoundException("Order Not Found"));
        UpdateOrderServiceRequest request = new UpdateOrderServiceRequest(orderId, 5000L);
        assertThatThrownBy(() -> orderService.updateOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order Not Found");
    }

}