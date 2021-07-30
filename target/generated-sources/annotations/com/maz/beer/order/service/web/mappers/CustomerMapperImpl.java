package com.maz.beer.order.service.web.mappers;

import com.maz.beer.order.service.domain.Customer;
import com.maz.brewery.model.CustomerDto;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-07-30T17:16:59+0100",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.11 (Amazon.com Inc.)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Autowired
    private DateMapper dateMapper;

    @Override
    public Customer customerDtoToCustomer(CustomerDto customerDto) {
        if ( customerDto == null ) {
            return null;
        }

        Customer customer = new Customer();

        customer.setCustomerName( customerDto.getName() );
        customer.setId( customerDto.getId() );
        if ( customerDto.getVersion() != null ) {
            customer.setVersion( customerDto.getVersion().longValue() );
        }
        customer.setCreatedDate( dateMapper.asTimestamp( customerDto.getCreatedDate() ) );
        customer.setLastModifiedDate( dateMapper.asTimestamp( customerDto.getLastModifiedDate() ) );

        return customer;
    }

    @Override
    public CustomerDto customerToCustomerDto(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerDto customerDto = new CustomerDto();

        customerDto.setName( customer.getCustomerName() );
        customerDto.setId( customer.getId() );
        if ( customer.getVersion() != null ) {
            customerDto.setVersion( customer.getVersion().intValue() );
        }
        customerDto.setCreatedDate( dateMapper.asOffsetDateTime( customer.getCreatedDate() ) );
        customerDto.setLastModifiedDate( dateMapper.asOffsetDateTime( customer.getLastModifiedDate() ) );

        return customerDto;
    }
}
