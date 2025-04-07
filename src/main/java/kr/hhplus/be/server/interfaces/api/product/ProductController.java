package kr.hhplus.be.server.interfaces.api.product;

import kr.hhplus.be.server.application.product.dto.ProductBestResponse;
import kr.hhplus.be.server.application.product.dto.ProductDto;
import kr.hhplus.be.server.application.product.dto.ProductListResponse;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;

    @Override
    public ResponseEntity<?> getProduct(Long productId) {
        if (productId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing productId"));
        }

        ProductDto response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getProducts(int page, int size, String sort) {
        ProductListResponse response = new ProductListResponse(productService.getProductList(page, size, sort));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getBestProducts() {
        ProductBestResponse response = new ProductBestResponse(productService.getBestProducts());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> calculateBestProducts() {
        productService.calculateBestProducts();
        return ResponseEntity.ok().build();
    }
}
