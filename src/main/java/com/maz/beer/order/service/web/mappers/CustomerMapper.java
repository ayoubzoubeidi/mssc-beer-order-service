package com.maz.beer.order.service.web.mappers;

import com.maz.beer.order.service.domain.Customer;
import com.maz.brewery.model.CustomerDto;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

    @Mapping(target = "customerName", source = "name")
    Customer customerDtoToCustomer(CustomerDto customerDto);

    @InheritInverseConfiguration
    CustomerDto customerToCustomerDto(Customer customer);

}
