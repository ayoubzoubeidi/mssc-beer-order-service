package com.maz.beer.order.service.sm.actions;

import com.maz.beer.order.service.config.JmsConfig;
import com.maz.beer.order.service.domain.BeerOrder;
import com.maz.beer.order.service.domain.BeerOrderEventEnum;
import com.maz.beer.order.service.domain.BeerOrderStatusEnum;
import com.maz.beer.order.service.repositories.BeerOrderRepository;
import com.maz.beer.order.service.web.mappers.BeerOrderMapper;
import com.maz.brewery.model.BeerOrderDto;
import com.maz.brewery.model.events.DeallocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.maz.beer.order.service.services.BeerOrderManagerImpl.ORDER_ID_HEADER;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

        UUID orderId = UUID
                .fromString(
                        (String) Objects.requireNonNull(stateContext.getMessage().getHeaders().get(ORDER_ID_HEADER))
                );


        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(orderId);

        beerOrderOptional.ifPresentOrElse(
                (beerOrder -> {
                    BeerOrderDto beerOrderDto = beerOrderMapper.beerOrderToDto(beerOrder);
                    jmsTemplate.convertAndSend(JmsConfig.DEALLOCATE_ORDER_QUEUE,
                            DeallocateOrderRequest.builder().beerOrderDto(beerOrderDto).build());

                    log.debug("Sent Deallocation Request for order id " + orderId);
                })
                , () -> log.error("Order Not Found. Id: " + orderId));

    }
}
