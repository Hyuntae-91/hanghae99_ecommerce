package kr.hhplus.be.server.domain.product.dto;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import kr.hhplus.be.server.domain.order.model.OrderOption;
import kr.hhplus.be.server.domain.product.model.Product;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-11T00:47:29+0900",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductServiceResponse productToProductServiceResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        List<ProductOptionResponse> options = null;
        Long id = null;
        String name = null;
        Long price = null;
        int state = 0;
        String createdAt = null;

        options = toProductOptionResponseList( product.getOrderOptions() );
        id = product.getId();
        name = product.getName();
        price = product.getPrice();
        if ( product.getState() != null ) {
            state = product.getState();
        }
        createdAt = product.getCreatedAt();

        ProductServiceResponse productServiceResponse = new ProductServiceResponse( id, name, price, state, createdAt, options );

        return productServiceResponse;
    }

    @Override
    public List<ProductServiceResponse> productsToProductServiceResponses(List<Product> products) {
        if ( products == null ) {
            return null;
        }

        List<ProductServiceResponse> list = new ArrayList<ProductServiceResponse>( products.size() );
        for ( Product product : products ) {
            list.add( productToProductServiceResponse( product ) );
        }

        return list;
    }

    @Override
    public ProductOptionResponse toProductOptionResponse(OrderOption orderOption) {
        if ( orderOption == null ) {
            return null;
        }

        Long optionId = null;
        int stock = 0;
        int size = 0;

        optionId = orderOption.getId();
        if ( orderOption.getStockQuantity() != null ) {
            stock = orderOption.getStockQuantity();
        }
        if ( orderOption.getSize() != null ) {
            size = orderOption.getSize();
        }

        ProductOptionResponse productOptionResponse = new ProductOptionResponse( optionId, size, stock );

        return productOptionResponse;
    }

    @Override
    public List<ProductOptionResponse> toProductOptionResponseList(List<OrderOption> orderOptions) {
        if ( orderOptions == null ) {
            return null;
        }

        List<ProductOptionResponse> list = new ArrayList<ProductOptionResponse>( orderOptions.size() );
        for ( OrderOption orderOption : orderOptions ) {
            list.add( toProductOptionResponse( orderOption ) );
        }

        return list;
    }
}
