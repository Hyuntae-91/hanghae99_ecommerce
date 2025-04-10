package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.dto.*;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
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

    public CartItemServiceResponse getCart(GetCartServiceRequest request) {
        List<OrderItem> cartItems = orderItemRepository.findCartByUserId(request.userId());

        List<CartItemResponse> cartList = cartItems.stream()
                .map(OrderItem::toCartItemResponse)
                .toList();

        long totalPrice = cartList.stream()
                .mapToLong(CartItemResponse::eachPrice)
                .sum();

        return new CartItemServiceResponse(cartList, totalPrice);
    }

    public AddCartServiceResponse addCartService(AddCartServiceRequest request) {
        OrderItem item = OrderItem.of(
                request.userId(),
                null,
                request.productId(),
                request.optionId(),
                request.eachPrice(),
                request.quantity()
        );

        orderItemRepository.save(item);

        List<OrderItem> cartItems = orderItemRepository.findCartByUserId(request.userId());

        List<CartItemResponse> cartList = cartItems.stream()
                .map(OrderItem::toCartItemResponse)
                .toList();

        long totalPrice = cartList.stream()
                .mapToLong(CartItemResponse::eachPrice)
                .sum();

        return new AddCartServiceResponse(cartList, totalPrice);
    }

    public CreateOrderServiceResponse createOrder(CreateOrderServiceRequest requestDto) {
        Order order = Order.of(requestDto.userId(), requestDto.couponIssueId(), 0L, 0);
        Order saved = orderRepository.save(order);
        if (saved.getOrderItems().isEmpty()) {
            throw new IllegalStateException("주문 항목이 존재하지 않습니다.");
        }
        Long orderId = saved.getId();

        Map<Long, Integer> quantityMap = requestDto.items().stream()
                .collect(Collectors.toMap(CreateOrderItemDto::itemId, CreateOrderItemDto::quantity));

        List<OrderItem> updatedItems = saved.getOrderItems().stream()
                .filter(item -> quantityMap.containsKey(item.getId()))
                .map(item -> OrderItem.of(
                        item.getUserId(),
                        orderId,
                        item.getProductId(),
                        item.getOptionId(),
                        item.getEachPrice(),
                        quantityMap.getOrDefault(item.getId(), item.getQuantity())
                ))
                .toList();

        orderItemRepository.saveAll(updatedItems);
        return new CreateOrderServiceResponse(orderId);
    }

    public void updateTotalPrice(UpdateOrderServiceRequest request) {
        Order order = orderRepository.getById(request.orderId());
        order.applyTotalPrice(request.totalPrice());
    }
}
