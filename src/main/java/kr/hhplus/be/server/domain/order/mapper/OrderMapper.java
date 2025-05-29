package kr.hhplus.be.server.domain.order.mapper;

import kr.hhplus.be.server.domain.order.dto.request.CreateOrderOptionDto;
import kr.hhplus.be.server.domain.order.dto.request.CreateOrderServiceRequest;
import kr.hhplus.be.server.domain.order.dto.request.UpdateOrderServiceRequest;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.payment.dto.event.PaymentCompletedEvent;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.interfaces.api.payment.dto.request.PaymentRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    default CreateOrderServiceRequest toServiceRequest(Long userId, PaymentRequest request) {
        List<CreateOrderOptionDto> options = request.products().stream()
                .map(p -> new CreateOrderOptionDto(p.optionId(), p.quantity()))
                .toList();

        return new CreateOrderServiceRequest(userId, request.couponId(), request.couponIssueId(), options);
    }

    default List<ProductOptionKeyDto> toProductOptionKeyDtoList(List<OrderItem> dtoList) {
        return dtoList.stream()
                .map(item -> new ProductOptionKeyDto(
                        item.getProductId(),
                        item.getOptionId(),
                        item.getId() // 이게 itemId로 사용됨
                ))
                .toList();
    }

    UpdateOrderServiceRequest toUpdateOrderRequest(PaymentCompletedEvent event);
}