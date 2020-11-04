package com.maz.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maz.beer.order.service.domain.BeerOrder;
import com.maz.beer.order.service.domain.BeerOrderLine;
import com.maz.beer.order.service.domain.BeerOrderStatusEnum;
import com.maz.beer.order.service.domain.Customer;
import com.maz.beer.order.service.repositories.BeerOrderRepository;
import com.maz.beer.order.service.repositories.CustomerRepository;
import com.maz.beer.order.service.services.beer.BeerServiceRestTemplate;
import com.maz.brewery.model.BeerDto;
import com.maz.brewery.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

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
    WireMockServer wireMockServer;

    @Autowired
    ObjectMapper objectMapper;


    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @TestConfiguration
    static class restTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder().customerName("Test Customer").build());
    }


    @Test
    @Transactional
    void testNewToAllocated() throws InterruptedException, JsonProcessingException {

        BeerDto beer = BeerDto.builder().upc("12345").build();

        BeerPagedList list = new BeerPagedList(Arrays.asList(beer));

        wireMockServer.stubFor(WireMock.get(BeerServiceRestTemplate.BEER_UPC_PATH + "12345")
        .willReturn(okJson(objectMapper.writeValueAsString(beer))));

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().until(waitStatusToChange(savedBeerOrder.getId(), BeerOrderStatusEnum.VALIDATION_PENDING));

        BeerOrder order = beerOrderRepository.findOneById(savedBeerOrder.getId());

        assertNotNull(savedBeerOrder);
        assertEquals(BeerOrderStatusEnum.VALIDATION_PENDING, order.getOrderStatus());

    }

    public Callable<Boolean> waitStatusToChange(UUID beerId, BeerOrderStatusEnum beerOrderStatus) {
        BeerOrder beerOrder = beerOrderRepository.findById(beerId).get();
        return () -> beerOrder.getOrderStatus() == beerOrderStatus;
    }

    public BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();

        Set<BeerOrderLine> beerOrderLines = new HashSet<>();
        beerOrderLines.add(BeerOrderLine.builder().beerId(beerId).upc("12345").build());

        beerOrder.setBeerOrderLines(beerOrderLines);

        return beerOrder;
    }
}
