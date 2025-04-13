package kr.hhplus.be.server.domain.product.dto;

import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "orderOptions", target = "options")
    ProductServiceResponse productToProductServiceResponse(Product product);

    List<ProductServiceResponse> productsToProductServiceResponses(List<Product> products);

    @Mapping(source = "id", target = "optionId")
    @Mapping(source = "stockQuantity", target = "stock")
    ProductOptionResponse toProductOptionResponse(OrderOption orderOption);

    List<ProductOptionResponse> toProductOptionResponseList(List<OrderOption> orderOptions);

    default List<Long> extractProductIds(List<ProductOptionKeyDto> dtoList) {
        if (dtoList == null) return List.of();
        return dtoList.stream()
                .map(ProductOptionKeyDto::productId)
                .toList();
    }
}