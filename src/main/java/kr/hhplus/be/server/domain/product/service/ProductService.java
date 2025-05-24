package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.dto.event.ProductTotalPriceRequestedEvent;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderOptionRepository;
import kr.hhplus.be.server.domain.product.assembler.ProductAssembler;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.model.ProductStates;
import kr.hhplus.be.server.domain.product.dto.request.ProductListServiceRequest;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.domain.product.dto.request.ProductServiceRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderOptionRepository orderOptionRepository;
    private final ProductAssembler productAssembler;
    private final ApplicationEventPublisher eventPublisher;

    public ProductServiceResponse getProductById(ProductServiceRequest requestDto) {
        Product product = productRepository.findById(requestDto.productId());
        List<OrderOption> orderOptions = orderOptionRepository.findByProductId(product.getId());
        return productAssembler.toResponseWithOptions(product, orderOptions);
    }

    public List<ProductServiceResponse> getProductByIds(List<ProductOptionKeyDto> requestDto) {
        List<Long> productIds = productAssembler.extractProductIds(requestDto);
        List<Product> productList = productRepository.findByIds(productIds);
        return productAssembler.toResponses(productList);
    }

    @Cacheable(
            value = "productList",
            key = "'productList::page=' + #root.args[0].page() + ':size=' + #root.args[0].size() + ':sort=' + #root.args[0].sort()"
    )
    public List<ProductServiceResponse> getProductList(ProductListServiceRequest requestDto) {
        List<Integer> excludedStates = ProductStates.excludedInProductList();

        List<Product> productList = productRepository.findByStateNotIn(
                requestDto.page(),
                requestDto.size(),
                requestDto.sort(),
                excludedStates
        );
        List<ProductServiceResponse> result = new ArrayList<>();
        for (Product product : productList) {
            List<OrderOption> options = orderOptionRepository.findByProductId(product.getId());
            ProductServiceResponse response = productAssembler.toResponseWithOptions(product, options);
            result.add(response);
        }

        return result;
    }

    public ProductTotalPriceResponse calculateTotalPrice(ProductTotalPriceRequestedEvent requestDto) {
        long total = 0;
        for (ProductOptionKeyDto dto : requestDto.items()) {
            OrderItem item = orderItemRepository.findById(dto.itemId());
            total += item.calculateTotalPrice();
        }

        return ProductTotalPriceResponse.from(total);
    }

    public void updateProductsScore(List<Long> productIds) {
        productRepository.updateProductsScore(productIds);
    }
}