package com.maz.beer.order.service.listeners;

import com.maz.beer.order.service.config.JmsConfig;
import com.maz.brewery.model.events.OrderValidationResponse;
import com.maz.brewery.model.events.ValidateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderValidationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listenValidation(Message message) {

        ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) message.getPayload();

        Boolean isValid = !request.getBeerOrder().getCustomerRef().equalsIgnoreCase("validation exception");

        OrderValidationResponse response = OrderValidationResponse
                .builder()
                .orderId(request.getBeerOrder().getId())
                .isValid(isValid).build();
        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE ,response);

    }


}
