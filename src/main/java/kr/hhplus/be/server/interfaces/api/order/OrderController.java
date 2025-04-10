package kr.hhplus.be.server.interfaces.api.order;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.application.order.dto.AddCartFacadeRequest;
import kr.hhplus.be.server.domain.order.dto.AddCartServiceResponse;
import kr.hhplus.be.server.domain.order.dto.CartItemResponse;
import kr.hhplus.be.server.interfaces.api.order.dto.AddCartRequest;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.order.dto.AddCartResponse;
import kr.hhplus.be.server.interfaces.api.order.dto.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    @Override
    public ResponseEntity<?> addToCart(
            @RequestHeader("userId") Long userId,
            @RequestBody @Valid AddCartRequest request
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }
        AddCartFacadeRequest addCartServiceRequest = new AddCartFacadeRequest(
                userId, request.productId(), request.optionId(), request.quantity()
        );
        AddCartServiceResponse result = orderFacade.addCart(addCartServiceRequest);
        List<CartItem> cartList = result.cartList().stream()
                .map(CartItem::from)
                .toList();

        return ResponseEntity.ok(new AddCartResponse(cartList, result.totalPrice()));
    }
}
