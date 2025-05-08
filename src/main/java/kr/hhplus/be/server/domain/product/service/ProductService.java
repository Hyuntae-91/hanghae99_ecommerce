package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.product.dto.response.ProductOptionResponse;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.ProductStates;
import kr.hhplus.be.server.domain.product.dto.*;
import kr.hhplus.be.server.domain.product.dto.request.ProductListServiceRequest;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.domain.product.dto.request.ProductServiceRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderOptionRepository orderOptionRepository;
    private final ProductMapper productMapper;

    private ProductServiceResponse toResponseWithOptions(Product product) {
        List<OrderOption> orderOptions = orderOptionRepository.findByProductId(product.getId());
        List<ProductOptionResponse> optionResponses = productMapper.toProductOptionResponseList(orderOptions);
        return productMapper.productToProductServiceResponse(product).withOptions(optionResponses);
    }

    public ProductServiceResponse getProductById(ProductServiceRequest requestDto) {
        Product product = productRepository.findById(requestDto.productId());

        List<OrderOption> orderOptions = orderOptionRepository.findByProductId(product.getId());
        List<ProductOptionResponse> optionResponses = productMapper.toProductOptionResponseList(orderOptions);

        ProductServiceResponse baseResponse = productMapper.productToProductServiceResponse(product);
        return baseResponse.withOptions(optionResponses);
    }

    public List<ProductServiceResponse> getProductByIds(List<ProductOptionKeyDto> requestDto) {
        List<Product> productList = productRepository.findByIds(productMapper.extractProductIds(requestDto));
        return productMapper.productsToProductServiceResponses(productList);
    }

    @Cacheable(
            value = "productList",
            key = "'productList::page=' + #requestDto.page() + ':size=' + #requestDto.size() + ':sort=' + #requestDto.sort()"
    )
    public List<ProductServiceResponse> getProductList(ProductListServiceRequest requestDto) {
        List<Integer> excludedStates = List.of(
                ProductStates.DELETED.getCode(),
                ProductStates.SOLD_OUT.getCode()
        );

        List<Product> productList = productRepository.findByStateNotIn(
                requestDto.page(),
                requestDto.size(),
                requestDto.sort(),
                excludedStates
        );

        return productList.stream()
                .map(this::toResponseWithOptions)
                .toList();
    }

    @Cacheable(value = "bestProducts", key = "'bestProducts'")
    public List<ProductServiceResponse> getBestProducts() {
        log.info("============ " + "[Cache miss]" + " ============");
        List<Product> bestProducts = productRepository.findPopularTop5();

        return bestProducts.stream()
                .map(this::toResponseWithOptions)
                .toList();
    }

    @CachePut(value = "bestProducts", key = "'bestProducts'")
    public List<ProductServiceResponse> calculateBestProducts() {
        log.info("[Scheduler] Calculating best products");
        List<Product> bestProducts = productRepository.findPopularTop5();

        return bestProducts.stream()
                .map(this::toResponseWithOptions)
                .toList();
    }

    public ProductTotalPriceResponse calculateTotalPrice(List<ProductOptionKeyDto> requestDto) {
        long total = 0;
        for (ProductOptionKeyDto dto : requestDto) {
            OrderItem item = orderItemRepository.findById(dto.itemId());
            total += item.calculateTotalPrice();
        }

        return new ProductTotalPriceResponse(total);
    }
}