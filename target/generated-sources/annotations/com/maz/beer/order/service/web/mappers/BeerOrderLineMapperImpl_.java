package com.maz.beer.order.service.web.mappers;

import com.maz.beer.order.service.domain.BeerOrderLine;
import com.maz.brewery.model.BeerOrderLineDto;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-08-04T14:19:18+0100",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.11 (Amazon.com Inc.)"
)
@Component
@Qualifier("delegate")
public class BeerOrderLineMapperImpl_ implements BeerOrderLineMapper {

    @Autowired
    private DateMapper dateMapper;

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        if ( line == null ) {
            return null;
        }

        BeerOrderLineDto beerOrderLineDto = new BeerOrderLineDto();

        beerOrderLineDto.setId( line.getId() );
        if ( line.getVersion() != null ) {
            beerOrderLineDto.setVersion( line.getVersion().intValue() );
        }
        beerOrderLineDto.setCreatedDate( dateMapper.asOffsetDateTime( line.getCreatedDate() ) );
        beerOrderLineDto.setLastModifiedDate( dateMapper.asOffsetDateTime( line.getLastModifiedDate() ) );
        beerOrderLineDto.setUpc( line.getUpc() );
        beerOrderLineDto.setBeerId( line.getBeerId() );
        beerOrderLineDto.setOrderQuantity( line.getOrderQuantity() );
        beerOrderLineDto.setQuantityAllocated( line.getQuantityAllocated() );

        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        if ( dto == null ) {
            return null;
        }

        BeerOrderLine beerOrderLine = new BeerOrderLine();

        beerOrderLine.setId( dto.getId() );
        if ( dto.getVersion() != null ) {
            beerOrderLine.setVersion( dto.getVersion().longValue() );
        }
        beerOrderLine.setCreatedDate( dateMapper.asTimestamp( dto.getCreatedDate() ) );
        beerOrderLine.setLastModifiedDate( dateMapper.asTimestamp( dto.getLastModifiedDate() ) );
        beerOrderLine.setUpc( dto.getUpc() );
        beerOrderLine.setBeerId( dto.getBeerId() );
        beerOrderLine.setOrderQuantity( dto.getOrderQuantity() );
        beerOrderLine.setQuantityAllocated( dto.getQuantityAllocated() );

        return beerOrderLine;
    }
}
