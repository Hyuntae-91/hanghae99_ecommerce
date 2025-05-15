package kr.hhplus.be.server.interfaces.api.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.product.dto.request.PageableRequest;
import kr.hhplus.be.server.interfaces.api.product.dto.response.ProductResponse;
import kr.hhplus.be.server.interfaces.api.product.dto.request.ProductsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/v1/products")
@Tag(name = "Product", description = "상품 관련 API")
public interface ProductApi {

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product Not Found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{productId}")
    ResponseEntity<?> getProduct(@PathVariable("productId") Long productId);


    @Operation(summary = "상품 리스트 조회", description = "상품 리스트를 페이지, 정렬 기준에 따라 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    })
    @GetMapping
    ResponseEntity<?> getProducts(
            @RequestBody ProductsRequest request
    );

    @Operation(summary = "일간 인기 상품 조회", description = "Daily 인기 상품 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    })
    @GetMapping("/bests")
    ResponseEntity<?> getDailyBestProducts(PageableRequest request);

    @Operation(summary = "주간 인기 상품 조회", description = "Weekly 인기 상품 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    })
    @GetMapping("/weekly-bests")
    ResponseEntity<?> getWeeklyBestProducts(PageableRequest request);

}
