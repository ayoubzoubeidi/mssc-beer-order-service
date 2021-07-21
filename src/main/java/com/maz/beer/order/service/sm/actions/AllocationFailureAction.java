package com.maz.beer.order.service.sm.actions;

import com.maz.beer.order.service.domain.BeerOrderEventEnum;
import com.maz.beer.order.service.domain.BeerOrderStatusEnum;
import com.maz.brewery.model.events.AllocationFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import java.util.Objects;
import java.util.UUID;

import static com.maz.beer.order.service.config.JmsConfig.ALLOCATE_FAILURE_QUEUE;
import static com.maz.beer.order.service.services.BeerOrderManagerImpl.ORDER_ID_HEADER;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

        UUID orderId = UUID
                .fromString(
                        (String) Objects.requireNonNull(stateContext.getMessage().getHeaders().get(ORDER_ID_HEADER))
                );

        jmsTemplate.convertAndSend(ALLOCATE_FAILURE_QUEUE, AllocationFailureEvent.builder().orderId(orderId).build());

        log.info("Allocation Failed for beer order " + orderId);
    }
}
