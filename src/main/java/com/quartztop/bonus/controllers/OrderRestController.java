package com.quartztop.bonus.controllers;

import com.quartztop.bonus.crm.Invoices;
import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.orders.OrderDto;
import com.quartztop.bonus.orders.StatusOrders;
import com.quartztop.bonus.repositoriesBonus.OrderRepository;
import com.quartztop.bonus.repositoriesBonus.StatusRepository;
import com.quartztop.bonus.repositoriesCrm.InvoicesRepository;
import com.quartztop.bonus.servises.orderService.OrderDtoService;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/order")
public class OrderRestController {

    private final OrderRepository orderRepository;
    private final UserCrudService userCrudService;
    private final StatusRepository statusRepository;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(Principal principal) {
        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username);

        List<Order> orders = orderRepository.getOrdersByUserEntity(user);
        List<OrderDto> userOrders = orders.stream().map(OrderDtoService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody Order order) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserEntity user = userCrudService
                .findByEmail(authentication.getName()); // authentication.getName() это адрес email

        order.setUserEntity(user);
        order.setCreateDate(LocalDateTime.now());

        order.setStatus(statusRepository.findByName("Создан"));

        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }
}
