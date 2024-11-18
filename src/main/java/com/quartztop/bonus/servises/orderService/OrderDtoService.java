package com.quartztop.bonus.servises.orderService;

import com.quartztop.bonus.orders.*;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OrderDtoService {

    public static OrderDto mapOrderToDto(Order order) {

        LocalDateTime dataCreateOrderDto = order.getCreateDate();

        // Форматирование даты и времени.
        DateTimeFormatter formatterMoment = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String moment = dataCreateOrderDto.format(formatterMoment);

        String orderPaymentType = "не установлен";
        if(order.getPaymentType() != null) {
            orderPaymentType = order.getPaymentType().getName();
        }

        String orderStatusName = "не установлен";
        String statusColor = "#dddddd";
        if(order.getStatus() != null) {
            orderStatusName = order.getStatus().getName();
            statusColor = order.getStatus().getColor();
        }

        OrderDto orderDto = new OrderDto();

        if(order.getStatus() != null) {
            StatusOrdersDto statusOrdersDto = StatusOrderCrudService.mapToDto(order.getStatus());
            orderDto.setStatusOrdersDto(statusOrdersDto);
        }
        if(order.getUserEntity() != null) {

            UserEntity userEntity = order.getUserEntity();
            UserDto userDto = UserCrudService.mapToDto(userEntity);
            UserDto managerDto = UserCrudService.mapToDto(userEntity.getManager());
            userDto.setManagerDto(managerDto);
            orderDto.setUserDto(userDto);
        }
        if(order.getBonusValue() != null) {
            BonusValueDto bonusValueDto = BonusValueCrudService.mapToDto(order.getBonusValue());
            orderDto.setBonusValueDto(bonusValueDto);
        }

        orderDto.setId(order.getId());
        orderDto.setCreateDate(moment);
        orderDto.setStatusName(orderStatusName);
        orderDto.setStatusColor(statusColor);
        orderDto.setPaymentType(orderPaymentType);
        orderDto.setSum(order.getSum());
        orderDto.setInvoiceNumber(order.getInvoiceNumber());
        orderDto.setInvoiceExternalId(order.getInvoiceExternalId());
        orderDto.setInvoiceDate(order.getInvoiceDate());
        orderDto.setProductExternalId(order.getProductExternalId());
        orderDto.setProductName(order.getProductName());
        orderDto.setProductQuantity(order.getProductQuantity());
        orderDto.setSumByInvoice(order.getSumByInvoice());

        return orderDto;
    }
}
