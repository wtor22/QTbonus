package com.quartztop.bonus.servises.orderService;

import com.quartztop.bonus.orders.*;
import com.quartztop.bonus.repositoriesBonus.OrderRepository;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;


    public List<Order> searchOrders(String fio) {
        Specification<Order> spec = Specification
                .where(OrderSpecifications.hasFio(fio));

        return orderRepository.findAll(spec);
    }

    public List<Order> getOrdersByTypeWithSort(Specification<Order> spec, String sortBy, boolean ascending) {
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return orderRepository.findAll(spec, sort);
    }

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
        orderDto.setDataPayment(order.getDataPayment());

        return orderDto;
    }

    public Page<Order> getOrdersByTypeWithSortAndPagination(String type, Pageable pageable) {
        return orderRepository.findByType(type, pageable);
    }
}
