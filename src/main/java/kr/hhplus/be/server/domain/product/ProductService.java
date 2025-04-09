package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.dto.ProductListServiceRequest;
import kr.hhplus.be.server.domain.product.dto.ProductListServiceResponse;
import kr.hhplus.be.server.domain.product.dto.ProductServiceRequest;
import kr.hhplus.be.server.domain.product.dto.ProductServiceResponse;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.product.dto.GetProductsRepositoryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductServiceResponse getProductById(ProductServiceRequest requestDto) {
        Product product = productRepository.findById(requestDto.productId());
        return ProductServiceResponse.from(product);
    }

    public ProductListServiceResponse getProductList(ProductListServiceRequest requestDto) {
        GetProductsRepositoryRequestDto reqRepository = new GetProductsRepositoryRequestDto(
                requestDto.page(), requestDto.size(), requestDto.sort()
        );
        List<Product> productList = productRepository.findAll(reqRepository);

        List<ProductServiceResponse> dtoList = productList.stream()
                .map(product -> {
                    if (product.getOrderOptions() == null) {
                        // orderOptions가 null이면 빈 리스트로 초기화
                        product.setOrderOptions(List.of());
                    }
                    return ProductServiceResponse.from(product);
                })
                .toList();

        return new ProductListServiceResponse(dtoList);
    }

    public ProductListServiceResponse getBestProducts() {
        List<Product> bestProducts = productRepository.findPopularTop5();
        List<ProductServiceResponse> dtoList = bestProducts.stream()
                .map(ProductServiceResponse::from)
                .toList();
        return new ProductListServiceResponse(dtoList);
    }

    public void calculateBestProducts() {
        productRepository.recalculateBestProducts();
    }
}