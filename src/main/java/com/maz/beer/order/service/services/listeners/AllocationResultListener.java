package com.maz.beer.order.service.services.listeners;

import com.maz.beer.order.service.config.JmsConfig;
import com.maz.beer.order.service.services.BeerOrderManager;
import com.maz.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult allocateOrderResult) {

        UUID beerOrderId = allocateOrderResult.getBeerOrder().getId();
        Boolean allocationError = allocateOrderResult.getAllocationError();
        Boolean pendingInventory = allocateOrderResult.getPendingInventory();

        beerOrderManager.processAllocationResult(beerOrderId, allocationError, pendingInventory);

    }



}
