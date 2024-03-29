package com.maz.beer.order.service.listeners;

import com.maz.beer.order.service.config.JmsConfig;
import com.maz.brewery.model.events.AllocateOrderRequest;
import com.maz.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest request) {

        Boolean allocationError = request.getBeerOrder().getCustomerRef().equals("allocation exception");

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResult.builder().beerOrder(request.getBeerOrder()).allocationError(allocationError).pendingInventory(false)
                .build());
    }
}
