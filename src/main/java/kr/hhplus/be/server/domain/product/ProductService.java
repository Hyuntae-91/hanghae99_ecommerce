package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.product.dto.*;
import kr.hhplus.be.server.domain.product.dto.request.ProductListServiceRequest;
import kr.hhplus.be.server.domain.product.dto.request.ProductOptionKeyDto;
import kr.hhplus.be.server.domain.product.dto.request.ProductServiceRequest;
import kr.hhplus.be.server.domain.product.dto.response.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.dto.response.ProductTotalPriceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceResponse getProductById(ProductServiceRequest requestDto) {
        Product product = productRepository.findById(requestDto.productId());
        return productMapper.productToProductServiceResponse(product);
    }

    public List<ProductServiceResponse> getProductByIds(List<ProductOptionKeyDto> requestDto) {
        List<Long> test = productMapper.extractProductIds(requestDto);
        List<Product> productList = productRepository.findByIds(productMapper.extractProductIds(requestDto));
        return productMapper.productsToProductServiceResponses(productList);
    }

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

        return productMapper.productsToProductServiceResponses(productList);
    }

    public List<ProductServiceResponse> getBestProducts() {
        List<Product> bestProducts = productRepository.findPopularTop5();
        return productMapper.productsToProductServiceResponses(bestProducts);
    }

    public void calculateBestProducts() {
        productRepository.recalculateBestProducts();
    }

    public ProductTotalPriceResponse calculateTotalPrice(List<ProductOptionKeyDto> requestDto) {
        // 1. 필요한 productId 추출 후 조회
        List<Product> productList = productRepository.findByIds(productMapper.extractProductIds(requestDto));

        // 2. 총 금액 계산
        long total = 0;
        for (Product product : productList) {
            for (OrderItem item : product.getOrderItems()) {
                boolean isMatched = requestDto.stream()
                        .anyMatch(req ->
                                req.productId().equals(product.getId()) &&
                                        req.optionId().equals(item.getOptionId())
                        );

                if (isMatched) {
                    total += item.calculateTotalPrice();
                }
            }
        }

        return new ProductTotalPriceResponse(total);
    }
}