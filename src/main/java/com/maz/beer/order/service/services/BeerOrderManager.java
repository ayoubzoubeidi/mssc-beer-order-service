package com.maz.beer.order.service.services;

import com.maz.beer.order.service.domain.BeerOrder;

public interface BeerOrderManager {
    BeerOrder newBeerOrder(BeerOrder beerOrder);
}
