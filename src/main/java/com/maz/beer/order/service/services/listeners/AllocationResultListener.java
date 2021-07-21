package com.maz.beer.order.service.services.listeners;

import com.maz.beer.order.service.config.JmsConfig;
import com.maz.beer.order.service.services.BeerOrderManager;
import com.maz.brewery.model.BeerOrderDto;
import com.maz.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult allocateOrderResult) {

        BeerOrderDto beerOrderDto = allocateOrderResult.getBeerOrder();
        Boolean allocationError = allocateOrderResult.getAllocationError();
        Boolean pendingInventory = allocateOrderResult.getPendingInventory();

        beerOrderManager.processAllocationResult(beerOrderDto, allocationError, pendingInventory);

    }



}
