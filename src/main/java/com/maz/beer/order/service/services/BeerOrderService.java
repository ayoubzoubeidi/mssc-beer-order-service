

package com.maz.beer.order.service.services;


import com.maz.brewery.model.BeerOrderDto;
import com.maz.brewery.model.BeerOrderPagedList;
import com.maz.brewery.model.CustomerDto;
import com.maz.brewery.model.CustomerPagedList;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

public interface BeerOrderService {
    BeerOrderPagedList listOrders(UUID customerId, Pageable pageable);

    BeerOrderDto placeOrder(UUID customerId, BeerOrderDto beerOrderDto);

    BeerOrderDto getOrderById(UUID customerId, UUID orderId);

    void pickupOrder(UUID customerId, UUID orderId);

    CustomerDto getCustomer(UUID customerId);

    CustomerPagedList getAllCustomers(Pageable pageable);
}
