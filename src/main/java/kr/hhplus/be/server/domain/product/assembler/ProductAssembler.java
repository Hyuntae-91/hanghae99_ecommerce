package kr.hhplus.be.server.domain.product.assembler;

import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.product.mapper.ProductMapper;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ProductAssembler {

    private final ProductMapper productMapper;

    public ProductAssembler(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public ProductServiceResponse toResponse(Product product) {
        return productMapper.productToProductServiceResponse(product);
    }

    public ProductServiceResponse toResponseWithOptions(Product product, List<OrderOption> orderOptions) {
        List<ProductOptionResponse> optionResponses = productMapper.toProductOptionResponseList(orderOptions);
        return productMapper.productToProductServiceResponse(product).withOptions(optionResponses);
    }

    public List<ProductServiceResponse> toResponses(List<Product> productList) {
        return productList.stream()
                .map(productMapper::productToProductServiceResponse)
                .toList();
    }

    public List<Long> extractProductIds(List<ProductOptionKeyDto> keyDtos) {
        return keyDtos.stream()
                .map(ProductOptionKeyDto::productId)
                .distinct()
                .toList();
    }

    public List<ProductServiceResponse> toSortedResponses(List<Product> products, List<Long> sortedIds) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return sortedIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .map(productMapper::productToProductServiceResponse)
                .toList();
    }
}
