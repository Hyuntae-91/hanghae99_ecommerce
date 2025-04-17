package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.dto.request.*;
import kr.hhplus.be.server.domain.order.dto.response.AddCartServiceResponse;
import kr.hhplus.be.server.domain.order.dto.response.CartItemResponse;
import kr.hhplus.be.server.domain.order.dto.response.CartItemServiceResponse;
import kr.hhplus.be.server.domain.order.dto.response.CreateOrderServiceResponse;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.exception.custom.OrderItemNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderOptionRepository orderOptionRepository;

    public CartItemServiceResponse getCart(GetCartServiceRequest request) {
        List<OrderItem> cartItems = orderItemRepository.findCartByUserId(request.userId());

        List<CartItemResponse> cartList = cartItems.stream()
                .map(item -> item.toCartItemResponse(
                        orderOptionRepository.getById(item.getOptionId())
                ))
                .toList();

        long totalPrice = cartList.stream()
                .mapToLong(cartItem -> cartItem.eachPrice() * cartItem.quantity())
                .sum();

        return new CartItemServiceResponse(cartList, totalPrice);
    }

    public AddCartServiceResponse addCartService(AddCartServiceRequest request) {
        OrderOption option = orderOptionRepository.getById(request.optionId());
        option.validateEnoughStock(request.quantity());
        OrderItem orderItem = OrderItem.of(
                request.userId(),
                request.productId(),
                request.optionId(),
                request.eachPrice(),
                request.quantity()
        );

        orderItemRepository.save(orderItem);

        List<OrderItem> cartItems = orderItemRepository.findCartByUserId(request.userId());

        List<CartItemResponse> cartList = cartItems.stream()
                .map(item -> item.toCartItemResponse(
                        orderOptionRepository.getById(item.getOptionId())
                ))
                .toList();

        long totalPrice = cartList.stream()
                .mapToLong(cartItem -> cartItem.eachPrice() * cartItem.quantity())
                .sum();

        return new AddCartServiceResponse(cartList, totalPrice);
    }

    public CreateOrderServiceResponse createOrder(CreateOrderServiceRequest requestDto) {
        List<OrderItem> cartItems = orderItemRepository.findCartByUserId(requestDto.userId());
        if (cartItems.isEmpty()) {
            throw new OrderItemNotFoundException("주문 항목이 존재하지 않습니다.");
        }

        Map<Long, Integer> quantityMap = requestDto.items().stream()
                .collect(Collectors.toMap(CreateOrderItemDto::itemId, CreateOrderItemDto::quantity));

        List<OrderItem> updatedItems = cartItems.stream()
                .filter(item -> quantityMap.containsKey(item.getId()))
                .map(item -> OrderItem.of(
                        item.getUserId(),
                        item.getProductId(),
                        item.getOptionId(),
                        item.getEachPrice(),
                        quantityMap.getOrDefault(item.getId(), item.getQuantity())
                ))
                .toList();

        Order order = Order.of(requestDto.userId(), requestDto.couponIssueId(), 0L, 0);
        Order saved = orderRepository.save(order);
        if (saved == null) {
            throw new OrderItemNotFoundException("주문 저장 실패");
        }
        updatedItems.forEach(item -> item.applyOrderId(saved.getId()));
        List<OrderItem> newUpdatedItems = orderItemRepository.saveAll(updatedItems);

        return new CreateOrderServiceResponse(saved.getId());
    }

    public void updateTotalPrice(UpdateOrderServiceRequest request) {
        Order order = orderRepository.getById(request.orderId());
        order.applyTotalPrice(request.totalPrice());
    }
}
