package kr.hhplus.be.server.interfaces.api.product;

import jakarta.validation.Valid;
import kr.hhplus.be.server.domain.product.dto.request.BestProductRequest;
import kr.hhplus.be.server.domain.product.service.ProductRankingService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.dto.request.ProductListServiceRequest;
import kr.hhplus.be.server.domain.product.dto.request.ProductServiceRequest;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.product.dto.request.PageableRequest;
import kr.hhplus.be.server.interfaces.api.product.dto.response.ProductResponse;
import kr.hhplus.be.server.interfaces.api.product.dto.request.ProductsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;
    private final ProductRankingService productRankingService;

    @Override
    public ResponseEntity<?> getProduct(@PathVariable("productId") Long productId) {
        if (productId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing productId"));
        }
        return ResponseEntity.ok(ProductResponse.from(
                productService.getProductById(new ProductServiceRequest(productId)))
        );
    }

    @Override
    @GetMapping
    public ResponseEntity<?> getProducts(@Valid @ModelAttribute ProductsRequest request) {
        ProductListServiceRequest reqController = new ProductListServiceRequest(
                request.cursorId(), request.size(), request.sort()
        );
        return ResponseEntity.ok(ProductResponse.fromList(productService.getProductList(reqController)));
    }

    @Override
    public ResponseEntity<?> getDailyBestProducts(PageableRequest request) {
        return ResponseEntity.ok(ProductResponse.fromList(
                productRankingService.getDailyBestProducts(new BestProductRequest(request.page(), request.size()))
        ));
    }

    @Override
    public ResponseEntity<?> getWeeklyBestProducts(PageableRequest request) {
        return ResponseEntity.ok(ProductResponse.fromList(
                productRankingService.getWeeklyBestProducts(new BestProductRequest(request.page(), request.size()))
        ));
    }

}
