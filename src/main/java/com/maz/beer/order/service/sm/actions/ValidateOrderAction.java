package com.maz.beer.order.service.sm.actions;

import com.maz.beer.order.service.config.JmsConfig;
import com.maz.beer.order.service.domain.BeerOrder;
import com.maz.beer.order.service.domain.BeerOrderEventEnum;
import com.maz.beer.order.service.domain.BeerOrderStatusEnum;
import com.maz.beer.order.service.repositories.BeerOrderRepository;
import com.maz.beer.order.service.web.mappers.BeerOrderMapper;
import com.maz.brewery.model.events.ValidateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import java.util.UUID;

import static com.maz.beer.order.service.services.BeerOrderManagerImpl.ORDER_ID_HEADER;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum>{

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

        String orderId = (String) context.getMessageHeader(ORDER_ID_HEADER);

        log.debug("Sending order validation request for order id: " + orderId);

        BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(orderId));

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE,
                new ValidateBeerOrderRequest(beerOrderMapper.beerOrderToDto(beerOrder)));

        log.debug("Validation request for order id: " + orderId + " is sent");

    }
}
