package com.quartztop.bonus.servises.orderService;

import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.orders.OrderDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderDtoService {

    public static OrderDto mapOrderToDto(Order order) {

        LocalDateTime dataCreateOrderDto =order.getCreateDate();

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

        return new OrderDto(order.getId(), moment, orderStatusName,
                statusColor, orderPaymentType, order.getSum(),
                order.getInvoiceNumber(), order.getInvoiceExternalId(), order.getInvoiceDate(),
                order.getProductExternalId(), order.getProductName(), order.getProductQuantity());

    }
}
