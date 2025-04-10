package kr.hhplus.be.server.application.payment.dto;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import kr.hhplus.be.server.domain.product.dto.ProductOptionKeyDto;
import kr.hhplus.be.server.interfaces.api.payment.dto.PaymentProductDto;
import kr.hhplus.be.server.interfaces.api.payment.dto.PaymentRequest;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-11T06:08:14+0900",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class PaymentFacadeMapperImpl implements PaymentFacadeMapper {

    @Override
    public PaymentProductFacadeDto toFacadeDto(PaymentProductDto dto) {
        if ( dto == null ) {
            return null;
        }

        Long productId = null;
        String name = null;
        Long optionId = null;
        Long itemId = null;
        Integer quantity = null;

        productId = dto.id();
        name = dto.name();
        optionId = dto.optionId();
        itemId = dto.itemId();
        quantity = dto.quantity();

        PaymentProductFacadeDto paymentProductFacadeDto = new PaymentProductFacadeDto( productId, name, optionId, itemId, quantity );

        return paymentProductFacadeDto;
    }

    @Override
    public List<PaymentProductFacadeDto> toFacadeDtoList(List<PaymentProductDto> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<PaymentProductFacadeDto> list = new ArrayList<PaymentProductFacadeDto>( dtoList.size() );
        for ( PaymentProductDto paymentProductDto : dtoList ) {
            list.add( toFacadeDto( paymentProductDto ) );
        }

        return list;
    }

    @Override
    public PaymentFacadeRequest toFacadeRequest(PaymentRequest request) {
        if ( request == null ) {
            return null;
        }

        List<PaymentProductFacadeDto> products = null;
        Long couponIssueId = null;

        products = toFacadeDtoList( request.products() );
        couponIssueId = request.couponIssueId();

        Long userId = null;

        PaymentFacadeRequest paymentFacadeRequest = new PaymentFacadeRequest( userId, products, couponIssueId );

        return paymentFacadeRequest;
    }

    @Override
    public ProductOptionKeyDto toProductOptionKey(PaymentProductFacadeDto dto) {
        if ( dto == null ) {
            return null;
        }

        Long productId = null;
        Long optionId = null;
        Long itemId = null;

        productId = dto.productId();
        optionId = dto.optionId();
        itemId = dto.itemId();

        ProductOptionKeyDto productOptionKeyDto = new ProductOptionKeyDto( productId, optionId, itemId );

        return productOptionKeyDto;
    }

    @Override
    public List<ProductOptionKeyDto> toProductOptionKeyList(List<PaymentProductFacadeDto> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<ProductOptionKeyDto> list = new ArrayList<ProductOptionKeyDto>( dtoList.size() );
        for ( PaymentProductFacadeDto paymentProductFacadeDto : dtoList ) {
            list.add( toProductOptionKey( paymentProductFacadeDto ) );
        }

        return list;
    }
}
