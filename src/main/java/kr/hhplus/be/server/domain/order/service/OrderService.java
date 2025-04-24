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
import kr.hhplus.be.server.exception.custom.AlreadyOrderedException;
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

    private record CartItemResponseBundle(List<CartItemResponse> items, long totalPrice) {}

    private CartItemResponseBundle buildCartResponse(Long userId) {
        List<OrderItem> cartItems = orderItemRepository.findCartByUserId(userId);

        List<CartItemResponse> cartList = cartItems.stream()
                .map(item -> item.toCartItemResponse(
                        orderOptionRepository.getById(item.getOptionId())
                ))
                .toList();

        long totalPrice = cartList.stream()
                .mapToLong(cartItem -> cartItem.eachPrice() * cartItem.quantity())
                .sum();

        return new CartItemResponseBundle(cartList, totalPrice);
    }

    @Transactional
    public CartItemServiceResponse getCart(GetCartServiceRequest request) {
        CartItemResponseBundle bundle = buildCartResponse(request.userId());
        return new CartItemServiceResponse(bundle.items, bundle.totalPrice());
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
        CartItemResponseBundle bundle = buildCartResponse(request.userId());
        return new AddCartServiceResponse(bundle.items, bundle.totalPrice());
    }

    public CreateOrderServiceResponse createOrder(CreateOrderServiceRequest requestDto) {
        // 1. 유효성 검사된 요청에서 optionId 목록과 수량 Map 추출
        List<Long> optionIds = requestDto.extractOptionIds();
        Map<Long, Integer> quantityMap = requestDto.toQuantityMap();

        // 2. OrderOption에 대해 먼저 비관적 락(PESSIMISTIC_WRITE)으로 조회
        Map<Long, OrderOption> lockedOptionMap = optionIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        orderOptionRepository::findWithLockById
                ));

        // 3. 유저 장바구니에서 OrderItem 조회
        List<OrderItem> cartItems = orderItemRepository.findCartByUserId(requestDto.userId());
        if (cartItems.isEmpty()) {
            throw new OrderItemNotFoundException("주문 항목이 존재하지 않습니다.");
        }

        // 4. 사용자가 주문하려는 optionId 기준으로 필터링
        List<OrderItem> selectedItems = cartItems.stream()
                .filter(item -> lockedOptionMap.containsKey(item.getOptionId()))
                .toList();

        if (selectedItems.isEmpty()) {
            throw new OrderItemNotFoundException("선택한 옵션에 해당하는 장바구니 항목이 없습니다.");
        }

        // 5. 중복 주문 여부 확인
        boolean hasDuplicate = selectedItems.stream().anyMatch(item -> item.getOrderId() != null);
        if (hasDuplicate) {
            throw new AlreadyOrderedException("이미 주문된 항목이 포함되어 있습니다.");
        }

        // 6. Order 생성 및 저장
        Order order = Order.of(requestDto.userId(), requestDto.couponIssueId(), 0L, 0);
        Order savedOrder = orderRepository.save(order);

        // 7. 각 OrderItem 업데이트 및 재고 차감
        for (OrderItem item : selectedItems) {
            Long optionId = item.getOptionId();
            OrderOption lockedOption = lockedOptionMap.get(optionId);
            int qty = quantityMap.getOrDefault(optionId, item.getQuantity());

            lockedOption.decreaseStock(qty);
            item.applyQuantity(qty);
            item.applyOrderId(savedOrder.getId());
        }

        orderItemRepository.saveAll(selectedItems);
        return new CreateOrderServiceResponse(savedOrder.getId());
    }

    public void updateTotalPrice(UpdateOrderServiceRequest request) {
        Order order = orderRepository.getById(request.orderId());
        order.applyTotalPrice(request.totalPrice());
    }
}
