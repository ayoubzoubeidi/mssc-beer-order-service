package com.maz.beer.order.service.services;

import com.maz.beer.order.service.domain.BeerOrder;
import com.maz.brewery.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {
    BeerOrder newBeerOrder(BeerOrder beerOrder);
    void processValidationResult(UUID beerOrderId, Boolean isValid);
    void processAllocationResult(BeerOrderDto beerOrderDto, Boolean allocationError, Boolean pendingInventory);
}
