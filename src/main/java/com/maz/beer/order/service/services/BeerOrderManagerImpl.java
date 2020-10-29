package com.maz.beer.order.service.services;

import com.maz.beer.order.service.domain.BeerOrder;
import com.maz.beer.order.service.domain.BeerOrderEventEnum;
import com.maz.beer.order.service.domain.BeerOrderStatusEnum;
import com.maz.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "beerOrderId";
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStatusInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {

        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedBeerOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {

        BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderId);

        BeerOrderEventEnum event;

        if (isValid) {
            event = BeerOrderEventEnum.VALIDATION_PASSED;

            BeerOrder validatedOrder = beerOrder = beerOrderRepository.findOneById(beerOrderId);

            sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
        }
        else {
            event = BeerOrderEventEnum.VALIDATION_FAILED;
        }

        sendBeerOrderEvent(beerOrder, event);

    }

    @Override
    public void processAllocationResult(UUID beerOrderId, Boolean allocationError, Boolean pendingInventory) {

        BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderId);

        if (allocationError)
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        else if (pendingInventory)
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
        else sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);

    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {

        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = buildStateMachine(beerOrder);

        Message message = MessageBuilder.withPayload(event)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        sm.sendEvent(message);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> buildStateMachine(BeerOrder beerOrder) {

        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor().doWithAllRegions(
                sma -> {
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
                    sma.addStateMachineInterceptor(beerOrderStatusInterceptor);
                }
        );

        return sm;
    }
}
