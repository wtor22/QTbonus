package com.quartztop.bonus.servises.orderService;

import com.quartztop.bonus.orders.StatusOrders;
import com.quartztop.bonus.orders.StatusOrdersDto;
import org.springframework.stereotype.Service;

@Service
public class StatusOrderCrudService {

    public static StatusOrdersDto mapToDto(StatusOrders statusOrders) {

        return new StatusOrdersDto(statusOrders.getId(),
                statusOrders.getColor(),
                statusOrders.getName());
    }

}
