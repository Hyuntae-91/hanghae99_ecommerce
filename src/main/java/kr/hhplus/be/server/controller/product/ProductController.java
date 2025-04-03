package kr.hhplus.be.server.controller.product;

import kr.hhplus.be.server.dto.ErrorResponse;
import kr.hhplus.be.server.dto.product.ProductBestResponse;
import kr.hhplus.be.server.dto.product.ProductListResponse;
import kr.hhplus.be.server.dto.product.ProductResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController implements ProductApi {

    @Override
    public ResponseEntity<?> getProduct(Long productId) {
        if (productId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Product Not Found"));
        }

        ProductResponse response = new ProductResponse(
                productId,
                "상품상세",
                1000L,
                1L,
                "2025-04-03T09:00:00"
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getProducts(int page, int size, String sort) {
        List<ProductResponse> products = List.of(
                new ProductResponse(1L, "상품상세", 1000L, 1L, "2025-04-03T09:00:00"),
                new ProductResponse(2L, "상품상세", 1000L, 1L, "2025-04-03T09:00:00"),
                new ProductResponse(3L, "상품상세", 1000L, 1L, "2025-04-03T09:00:00")
        );

        return ResponseEntity.ok(new ProductListResponse(products));
    }

    @Override
    public ResponseEntity<?> getBestProducts() {
        List<ProductResponse> products = List.of(
                new ProductResponse(1L, "상품상세", 1000L, 1L, "2025-04-03T09:00:00"),
                new ProductResponse(2L, "상품상세", 1000L, 1L, "2025-04-03T09:00:00"),
                new ProductResponse(3L, "상품상세", 1000L, 1L, "2025-04-03T09:00:00")
        );

        return ResponseEntity.ok(new ProductBestResponse(products));
    }

    @Override
    public ResponseEntity<Void> calculateBestProducts() {
        // mock: 실제로는 판매량을 기반으로 상위 5개 상품을 계산해서 캐시나 DB에 저장
        return ResponseEntity.ok().build();
    }
}
