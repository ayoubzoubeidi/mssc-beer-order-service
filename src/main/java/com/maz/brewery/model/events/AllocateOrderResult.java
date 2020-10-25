package com.maz.brewery.model.events;

import com.maz.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocateOrderResult implements Serializable {

    static final long serialVersionUID = -3438116940660436460L;
    private BeerOrderDto beerOrder;
    private Boolean allocationError;
    private Boolean pendingInventory;

}
