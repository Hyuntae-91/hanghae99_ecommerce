package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.product.dto.*;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.product.dto.GetProductsRepositoryRequestDto;
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

    public ProductListServiceDto getProductByIds(ProductListSvcByIdsRequest requestDto) {
        List<Long> productIds = requestDto.items().stream()
                .map(ProductOptionKeyDto::productId)
                .distinct()
                .toList();
        List<Product> productList = productRepository.findByIds(productIds);
        List<ProductServiceResponse> dtoList = productMapper.productsToProductServiceResponses(productList);
        return new ProductListServiceDto(dtoList);
    }

    public ProductListServiceDto getProductList(ProductListServiceRequest requestDto) {
        GetProductsRepositoryRequestDto reqRepository = new GetProductsRepositoryRequestDto(
                requestDto.page(), requestDto.size(), requestDto.sort()
        );
        List<Product> productList = productRepository.findAll(reqRepository);
        List<ProductServiceResponse> dtoList = productMapper.productsToProductServiceResponses(productList);

        return new ProductListServiceDto(dtoList);
    }

    public ProductListServiceDto getBestProducts() {
        List<Product> bestProducts = productRepository.findPopularTop5();
        List<ProductServiceResponse> dtoList = productMapper.productsToProductServiceResponses(bestProducts);
        return new ProductListServiceDto(dtoList);
    }

    public void calculateBestProducts() {
        productRepository.recalculateBestProducts();
    }

    public ProductTotalPriceResponse calculateTotalPrice(ProductListSvcByIdsRequest requestDto) {
        // 1. 필요한 productId 추출 후 조회
        List<Long> productIds = requestDto.items().stream()
                .map(ProductOptionKeyDto::productId)
                .distinct()
                .toList();

        List<Product> productList = productRepository.findByIds(productIds);

        // 2. 총 금액 계산
        long total = requestDto.items().stream()
                .flatMap(optionKey -> productList.stream()
                        .filter(p -> p.getId().equals(optionKey.productId()))
                        .flatMap(p -> p.getOrderItems().stream())
                        .filter(item ->
                                item.getProductId().equals(optionKey.productId()) &&
                                        item.getOptionId().equals(optionKey.optionId())
                        )
                )
                .mapToLong(OrderItem::calculateTotalPrice)
                .sum();

        return new ProductTotalPriceResponse(total);
    }
}