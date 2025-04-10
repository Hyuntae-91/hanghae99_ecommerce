package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.dto.AddCartServiceRequest;
import kr.hhplus.be.server.domain.order.dto.AddCartServiceResponse;
import kr.hhplus.be.server.domain.order.dto.CartItemResponse;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AddCartServiceResponse AddCartService(AddCartServiceRequest request) {
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
}
