package com.maz.beer.order.service.services;

import com.maz.beer.order.service.domain.BeerOrder;

import java.util.UUID;

public interface BeerOrderManager {
    BeerOrder newBeerOrder(BeerOrder beerOrder);
    void processValidationResult(UUID beerOrderId, Boolean isValid);
    void processAllocationResult(UUID beerOrderId, Boolean allocationError, Boolean pendingInventory);
}
