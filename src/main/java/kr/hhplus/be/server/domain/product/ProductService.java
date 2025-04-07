package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.application.product.dto.ProductDto;
import kr.hhplus.be.server.domain.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDto getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product Not Found"));
        return ProductDto.from(product);
    }

    public List<ProductDto> getProductList(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort));
        List<Product> productList = productRepository.findAll(pageable);

        return productList.stream()
                .map(ProductDto::from)
                .toList();
    }

    public List<ProductDto> getBestProducts() {
        List<Product> bestProducts = productRepository.findPopularTop5();
        return bestProducts.stream()
                .map(ProductDto::from)
                .toList();
    }

    public void calculateBestProducts() {
        productRepository.recalculateBestProducts();
    }
}