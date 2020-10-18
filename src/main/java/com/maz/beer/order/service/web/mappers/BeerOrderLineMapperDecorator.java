package com.maz.beer.order.service.web.mappers;

import com.maz.beer.order.service.domain.BeerOrderLine;
import com.maz.beer.order.service.services.beer.BeerService;
import com.maz.brewery.model.BeerDto;
import com.maz.brewery.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {


    private BeerService beerService;
    private BeerOrderLineMapper mapper;

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Autowired
    public void setMapper(BeerOrderLineMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto dto = mapper.beerOrderLineToDto(line);
        Optional<BeerDto> beerDto = beerService.getBeerByUpc(line.getUpc());
        beerDto.ifPresent(
                beer -> {
                    dto.setBeerStyle(beer.getBeerStyle());
                    dto.setPrice(beer.getPrice());
                    dto.setBeerName(beer.getBeerName());
                    dto.setUpc(beer.getUpc());
                }
        );
        return dto;
    }

}
