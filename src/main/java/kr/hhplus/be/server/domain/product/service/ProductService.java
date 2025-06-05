package kr.hhplus.be.server.domain.product.service;

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
import kr.hhplus.be.server.interfaces.event.product.payload.ProductDataIds;
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
            key = "'productList::cursor=' + (#root.args[0].cursorId() == null ? 'null' : #root.args[0].cursorId()) + ':size=' + #root.args[0].size() + ':sort=' + #root.args[0].sort()"
    )
    public List<ProductServiceResponse> getProductList(ProductListServiceRequest requestDto) {
        List<Integer> excludedStates = ProductStates.excludedInProductList();

        List<Product> productList = productRepository.findByStateNotInCursor(
                requestDto.cursorId(),
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

    public ProductTotalPriceResponse calculateTotalPrice(List<ProductDataIds> requestDto) {
        long total = 0;
        for (ProductDataIds dto : requestDto) {
            OrderItem item = orderItemRepository.findById(dto.itemId());
            total += item.calculateTotalPrice();
        }

        return ProductTotalPriceResponse.from(total);
    }

    public void updateProductsScore(List<Long> productIds) {
        productRepository.updateProductsScore(productIds);
    }
}