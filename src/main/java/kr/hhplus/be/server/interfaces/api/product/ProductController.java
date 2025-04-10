package kr.hhplus.be.server.interfaces.api.product;

import jakarta.validation.Valid;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.product.dto.ProductListServiceDto;
import kr.hhplus.be.server.domain.product.dto.ProductListServiceRequest;
import kr.hhplus.be.server.domain.product.dto.ProductServiceRequest;
import kr.hhplus.be.server.domain.product.dto.ProductServiceResponse;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.product.dto.ProductListResponse;
import kr.hhplus.be.server.interfaces.api.product.dto.ProductResponse;
import kr.hhplus.be.server.interfaces.api.product.dto.ProductsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;

    @Override
    public ResponseEntity<?> getProduct(@PathVariable("productId") Long productId) {
        if (productId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing productId"));
        }
        ProductServiceRequest reqController = new ProductServiceRequest(productId);
        ProductServiceResponse result = productService.getProductById(reqController);
        return ResponseEntity.ok(ProductResponse.from(result));
    }

    @Override
    public ResponseEntity<?> getProducts(@RequestBody @Valid ProductsRequest request) {
        ProductListServiceRequest reqController = new ProductListServiceRequest(
                request.page(), request.size(), request.sort()
        );
        ProductListServiceDto result = productService.getProductList(reqController);
        return ResponseEntity.ok(ProductListResponse.from(result));
    }

    @Override
    public ResponseEntity<?> getBestProducts() {
        ProductListServiceDto result = productService.getBestProducts();
        return ResponseEntity.ok(ProductListResponse.from(result));
    }

    @Override
    public ResponseEntity<Void> calculateBestProducts() {
        productService.calculateBestProducts();
        return ResponseEntity.ok().build();
    }
}
