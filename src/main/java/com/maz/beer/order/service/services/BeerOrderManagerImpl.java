package com.maz.beer.order.service.services;

import com.maz.beer.order.service.domain.BeerOrder;
import com.maz.beer.order.service.domain.BeerOrderEventEnum;
import com.maz.beer.order.service.domain.BeerOrderStatusEnum;
import com.maz.beer.order.service.repositories.BeerOrderRepository;
import com.maz.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
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

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);

        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedBeerOrder;
    }

    @SneakyThrows
    @Transactional
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {

        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            if(isValid) {

                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

                BeerOrder validatedOrder = beerOrderRepository.findById(beerOrderId).get();

                sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);

            } else {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
            }
        }, () -> log.error("Order Not Found. Id: " + beerOrderId));


    }

    @Transactional
    @Override
    public void processAllocationResult(BeerOrderDto beerOrderDto, Boolean allocationError, Boolean pendingInventory) {

        UUID beerOrderId = beerOrderDto.getId();

        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            if (allocationError)
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
            else if (pendingInventory) {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
                updateQuantityAllocated(beerOrderDto);
            }
            else {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
                updateQuantityAllocated(beerOrderDto);
            }
        }, () -> log.error("Order Not Found. Id: " + beerOrderId));

    }

    @Transactional
    @Override
    public void processOrderPickedUp(UUID beerOrderId) {
        beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.PICKED_UP);
        }, () -> log.error("Order Not Found. Id: " + beerOrderId));
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

        sm.start();

        return sm;
    }

    private void updateQuantityAllocated(BeerOrderDto beerOrderDto) {

        UUID beerOrderId = beerOrderDto.getId();

        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

        beerOrderOptional.ifPresentOrElse(beerOrder -> {

            beerOrder.getBeerOrderLines().forEach(
                    beerOrderLine -> {
                        beerOrderDto.getBeerOrderLines().forEach(
                                beerOrderLineDto -> {
                                    if (beerOrderLine.getId().equals(beerOrderLineDto.getId()))
                                        beerOrderLine.setQuantityAllocated(beerOrderLine.getQuantityAllocated());
                                }
                        );
                    }
            );

            beerOrderRepository.saveAndFlush(beerOrder);

        }, () -> log.error("Order Not Found. Id: " + beerOrderId));

    }
}
