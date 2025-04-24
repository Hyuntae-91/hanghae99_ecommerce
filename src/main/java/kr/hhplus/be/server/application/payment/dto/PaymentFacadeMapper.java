package kr.hhplus.be.server.application.payment.dto;

import kr.hhplus.be.server.domain.coupon.dto.request.ApplyCouponDiscountServiceRequest;
import kr.hhplus.be.server.domain.order.dto.request.CreateOrderServiceRequest;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.interfaces.api.payment.dto.request.PaymentProductDto;
import kr.hhplus.be.server.interfaces.api.payment.dto.request.PaymentRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentFacadeMapper {

    @Mapping(source = "id", target = "productId")
    PaymentProductFacadeDto toFacadeDto(PaymentProductDto dto);

    List<PaymentProductFacadeDto> toFacadeDtoList(List<PaymentProductDto> dtoList);

    @Mapping(source = "products", target = "products")
    @Mapping(source = "couponIssueId", target = "couponIssueId")
    @Mapping(target = "userId", ignore = true)
    PaymentFacadeRequest toFacadeRequest(PaymentRequest request);

    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "optionId", target = "optionId")
    ProductOptionKeyDto toProductOptionKey(PaymentProductFacadeDto dto);

    List<ProductOptionKeyDto> toProductOptionKeyList(List<PaymentProductFacadeDto> dtoList);

    @Mapping(source = "products", target = "options")
    CreateOrderServiceRequest toServiceRequest(PaymentFacadeRequest request);

    @Mapping(source = "totalPrice", target = "originalPrice")
    ApplyCouponDiscountServiceRequest toApplyCouponDiscountServiceRequest(Long couponIssueId, Long totalPrice);

}

