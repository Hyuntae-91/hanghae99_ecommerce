package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.application.order.dto.AddCartFacadeRequest;
import kr.hhplus.be.server.domain.order.dto.request.AddCartServiceRequest;
import kr.hhplus.be.server.domain.order.dto.response.AddCartServiceResponse;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.product.dto.request.ProductServiceRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.interfaces.api.order.dto.request.AddCartRequest;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final PointService pointService;
    private final ProductService productService;
    private final OrderService orderService;

    public AddCartServiceResponse addCart(AddCartFacadeRequest request) {
        UserPointServiceRequest userPointServiceRequest = new UserPointServiceRequest(request.userId());
        UserPointServiceResponse userPoint = pointService.getUserPoint(userPointServiceRequest);
        ProductServiceRequest productServiceRequest = new ProductServiceRequest(request.productId());
        ProductServiceResponse product = productService.getProductById(productServiceRequest);
        AddCartServiceRequest addCartServiceRequest = new AddCartServiceRequest(
                request.userId(),
                request.productId(),
                request.optionId(),
                product.price(),
                request.quantity()
        );
        return orderService.addCartService(addCartServiceRequest);
    }

    public void createOrder(AddCartRequest request) {}
}
