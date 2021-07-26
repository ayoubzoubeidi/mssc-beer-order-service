

package com.maz.beer.order.service.services;

import com.maz.beer.order.service.domain.BeerOrder;
import com.maz.beer.order.service.domain.Customer;
import com.maz.beer.order.service.repositories.BeerOrderRepository;
import com.maz.beer.order.service.repositories.CustomerRepository;
import com.maz.beer.order.service.web.mappers.BeerOrderMapper;
import com.maz.beer.order.service.web.mappers.CustomerMapper;
import com.maz.brewery.model.BeerOrderDto;
import com.maz.brewery.model.BeerOrderPagedList;
import com.maz.brewery.model.CustomerDto;
import com.maz.brewery.model.CustomerPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderServiceImpl implements BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final CustomerMapper customerMapper;
    private final BeerOrderManager beerOrderManager;


    @Override
    public BeerOrderPagedList listOrders(UUID customerId, Pageable pageable) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isPresent()) {
            Page<BeerOrder> beerOrderPage =
                    beerOrderRepository.findAllByCustomer(customerOptional.get(), pageable);

            return new BeerOrderPagedList(beerOrderPage
                    .stream()
                    .map(beerOrderMapper::beerOrderToDto)
                    .collect(Collectors.toList()), PageRequest.of(
                    beerOrderPage.getPageable().getPageNumber(),
                    beerOrderPage.getPageable().getPageSize()),
                    beerOrderPage.getTotalElements());
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public BeerOrderDto placeOrder(UUID customerId, BeerOrderDto beerOrderDto) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (customerOptional.isPresent()) {
            BeerOrder beerOrder = beerOrderMapper.dtoToBeerOrder(beerOrderDto);
            beerOrder.setCustomer(customerOptional.get());
            beerOrder.getBeerOrderLines().forEach(line -> line.setBeerOrder(beerOrder));
            BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

            return beerOrderMapper.beerOrderToDto(savedBeerOrder);
        }
        //todo add exception type
        throw new RuntimeException("Customer Not Found");
    }

    @Override
    public BeerOrderDto getOrderById(UUID customerId, UUID orderId) {
        return beerOrderMapper.beerOrderToDto(getOrder(customerId, orderId));
    }

    @Override
    public void pickupOrder(UUID customerId, UUID orderId) {
        beerOrderManager.processOrderPickedUp(orderId);
    }

    @Override
    public CustomerDto getCustomer(UUID customerId) {

        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);

        if (optionalCustomer.isPresent()) {
            return customerMapper.customerToCustomerDto(optionalCustomer.get());
        } else {
            throw new RuntimeException("Customer Not Found");
        }

    }

    @Override
    public CustomerPagedList getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);

        return new CustomerPagedList(
                customers.stream().map(customerMapper::customerToCustomerDto).collect(Collectors.toList()),
                PageRequest.of(customers.getPageable().getPageNumber(), customers.getPageable().getPageSize()),
                customers.getTotalElements());
    }

    private BeerOrder getOrder(UUID customerId, UUID orderId){
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if(customerOptional.isPresent()){
            Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(orderId);

            if(beerOrderOptional.isPresent()){
                BeerOrder beerOrder = beerOrderOptional.get();

                // fall to exception if customer id's do not match - order not for customer
                if(beerOrder.getCustomer().getId().equals(customerId)){
                    return beerOrder;
                }
            }
            throw new RuntimeException("Beer Order Not Found");
        }
        throw new RuntimeException("Customer Not Found");
    }
}
