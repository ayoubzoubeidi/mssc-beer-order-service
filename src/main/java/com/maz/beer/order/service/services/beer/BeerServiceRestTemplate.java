package com.maz.beer.order.service.services.beer;

import com.maz.brewery.model.BeerDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@ConfigurationProperties(value = "maz.brewery", ignoreUnknownFields = false)
@Configuration
public class BeerServiceRestTemplate implements BeerService {

    public static final String BEER_PATH = "/api/v1/beer/";
    public static final String BEER_UPC_PATH = "/api/v1/beer/upc/";

    private final RestTemplate restTemplate;

    private String beerServiceHost;

    public BeerServiceRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String upc) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_UPC_PATH + upc, BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID beerId) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_PATH + beerId.toString(), BeerDto.class));
    }
}
