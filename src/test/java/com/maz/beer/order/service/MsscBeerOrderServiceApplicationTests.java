package com.maz.beer.order.service;

import com.maz.beer.order.service.config.TaskConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootTest
@ComponentScan(basePackages = "com.maz", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TaskConfig.class))
class MsscBeerOrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
