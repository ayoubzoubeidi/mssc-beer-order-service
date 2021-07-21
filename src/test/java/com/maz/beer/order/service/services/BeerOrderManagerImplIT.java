package com.maz.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maz.beer.order.service.config.JmsConfig;
import com.maz.beer.order.service.domain.BeerOrder;
import com.maz.beer.order.service.domain.BeerOrderLine;
import com.maz.beer.order.service.domain.BeerOrderStatusEnum;
import com.maz.beer.order.service.domain.Customer;
import com.maz.beer.order.service.repositories.BeerOrderRepository;
import com.maz.beer.order.service.repositories.CustomerRepository;
import com.maz.beer.order.service.services.beer.BeerServiceRestTemplate;
import com.maz.brewery.model.BeerDto;
import com.maz.brewery.model.events.AllocationFailureEvent;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.jgroups.util.Util.assertEquals;
import static org.jgroups.util.Util.assertNotNull;




@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerImplIT {

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    JmsTemplate jmsTemplate;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer(){
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }

    @TestConfiguration
    static class messagingConfiguration {
        @Bean
        public void config() throws Exception {
            ActiveMQServer server = ActiveMQServers.newActiveMQServer(new ConfigurationImpl()
                    .setPersistenceEnabled(false)
                    .setJournalDatasync(false)
                    .setSecurityEnabled(false)
                    .addAcceptorConfiguration("invm", "vm://0"));
            server.start();
        }
    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.saveAndFlush(Customer.builder()
                .customerName("Test Customer")
                .build());
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(WireMock.get(BeerServiceRestTemplate.BEER_UPC_PATH + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(savedBeerOrder2);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder2.getOrderStatus());

    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {

        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(WireMock.get(BeerServiceRestTemplate.BEER_UPC_PATH + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });



        beerOrderManager.processOrderPickedUp(savedBeerOrder.getId());

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
        });

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(savedBeerOrder2);
        assertEquals(BeerOrderStatusEnum.PICKED_UP, savedBeerOrder2.getOrderStatus());

    }

    @Test
    void testFailedValidation() throws JsonProcessingException {

        BeerDto beerDto = BeerDto.builder().upc("12345").build();

        wireMockServer.stubFor(WireMock.get(BeerServiceRestTemplate.BEER_UPC_PATH + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();

        beerOrder.setCustomerRef("validation exception");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
        });

        BeerOrder beerOrder1 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        BeerOrderStatusEnum testStatus = beerOrder1.getOrderStatus();

        assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, testStatus);

    }

    @Test
    void testFailedAllocation() throws JsonProcessingException{

        BeerDto beerDto = BeerDto.builder().upc("12345").build();

        wireMockServer.stubFor(WireMock.get(BeerServiceRestTemplate.BEER_UPC_PATH + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();

        beerOrder.setCustomerRef("allocation exception");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
        });


        AllocationFailureEvent allocationFailureEvent =
                (AllocationFailureEvent) jmsTemplate.receiveAndConvert(JmsConfig.ALLOCATE_FAILURE_QUEUE);

        assertEquals(savedBeerOrder.getId(), allocationFailureEvent.getOrderId());

    }

    public BeerOrder createBeerOrder(){
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .customerRef("customer")
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                .beerId(beerId)
                .upc("12345")
                .orderQuantity(8)
                .beerOrder(beerOrder)
                .build());

        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}
