package com.quartztop.bonus.controllers;

import com.quartztop.bonus.crm.Invoices;
import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.orders.OrderDto;
import com.quartztop.bonus.orders.StatusOrders;
import com.quartztop.bonus.orders.UploadedImages;
import com.quartztop.bonus.repositoriesBonus.OrderRepository;
import com.quartztop.bonus.repositoriesBonus.StatusRepository;
import com.quartztop.bonus.repositoriesBonus.UploadedImagesRepository;
import com.quartztop.bonus.repositoriesCrm.InvoicesRepository;
import com.quartztop.bonus.servises.FileService;
import com.quartztop.bonus.servises.orderService.OrderDtoService;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/order")
public class OrderRestController {

    private final OrderRepository orderRepository;
    private final UploadedImagesRepository uploadedImagesRepository;
    private final UserCrudService userCrudService;
    private final StatusRepository statusRepository;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllUserOrders(Principal principal) {
        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();

        List<Order> orders = orderRepository.getOrdersByUserEntity(user);
        List<OrderDto> userOrders = orders.stream().map(OrderDtoService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }

    // Получить ордер по id
    @GetMapping("/get-order")
    public ResponseEntity<?> getUserOrderById(@RequestParam("id") int orderId, Principal principal) {

        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();

        Order order = orderRepository.getOrderByUserEntityAndId(user,orderId);
        if(order == null) {
            String message = "Ордер не найден";
            Map<String, String> response = Map.of("message", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        OrderDto orderDto = OrderDtoService.mapOrderToDto(order);
        return ResponseEntity.ok(orderDto);
    }

    // Получить ордер Для Менеджера по id
    @GetMapping("/get-order-by-manager")
    public ResponseEntity<?> getOrderForManagerById(@RequestParam("id") int orderId, Principal principal) {

        log.error("RUN CONTROLLER - ID = " + orderId);
        String username = principal.getName();
        UserEntity manager = userCrudService.findByEmail(username).orElseThrow();

        Order order = orderRepository.findById(orderId).orElseThrow();
        UserEntity userEntity = order.getUserEntity();
        if(userEntity.getManager().getId() != manager.getId()) {
            String message = "НЕ ТВОЙ ЮЗЕР БЛЯ...";
            Map<String, String> response = Map.of("message", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if(order == null ) {
            String message = "Ордер не найден";
            Map<String, String> response = Map.of("message", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        OrderDto orderDto = OrderDtoService.mapOrderToDto(order);
        log.error("ORDERDTO ID = " + orderDto.getId());
        return ResponseEntity.ok(orderDto);
    }

    // Получить список заказов бонусов
    @GetMapping("/get-orders")
    public ResponseEntity<List<OrderDto>> getUserOrders(Principal principal) {

        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();

        List<Order> orders = orderRepository.getOrdersByUserEntityAndType(user, "bonus");
        List<OrderDto> userOrders = orders.stream().map(OrderDtoService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }
    // Получить список заказов бонусов по партнерам менеджера
    @GetMapping("/get-orders-by-manager")
    public ResponseEntity<List<OrderDto>> getManagerOrders(Principal principal) {

        log.error("START CONTROLLER");

        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();



        List<UserEntity> usersByManagerList = userCrudService.getAllUsersEntityByManager(user);
        List<Order> orders = orderRepository.getOrdersByUserEntityInAndType(usersByManagerList,"bonus");
        List<OrderDto> userOrders = orders.stream().map(OrderDtoService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }

    // Получить список акций Пользователя
    @GetMapping("/get-actions")
    public ResponseEntity<List<OrderDto>> getUserActions(Principal principal) {

        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();

        List<Order> actions = orderRepository.getOrdersByUserEntityAndType(user, "action");
        List<OrderDto> userOrders = actions.stream().map(OrderDtoService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(HttpServletRequest request, @RequestBody Order order) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserEntity user = userCrudService
                .findByEmail(authentication.getName()).orElseThrow(); // authentication.getName() это адрес email

        order.setUserEntity(user);
        order.setCreateDate(LocalDateTime.now());

        order.setStatus(statusRepository.findByName("Создан"));

        orderRepository.save(order);

        // Получаем сессию и файлы, загруженные в нее
        HttpSession session = request.getSession();

        List<String> tempFilePaths = (List<String>) session.getAttribute("uploadedFiles");


        // Проверяем, есть ли в сессии временные файлы
        if (tempFilePaths != null && !tempFilePaths.isEmpty()) {
            fileService.moveFileAsync(tempFilePaths, order);
            // Генерируем директорию для файлов заявки
            String orderDirPath = "C:\\bonus\\app\\uploads\\orders\\" + order.getId(); // Папка для файлов этой заявки
            File orderDir = new File(orderDirPath);
            if (!orderDir.exists()) {
                orderDir.mkdirs(); // Создаем папку, если она не существует
            }
            UploadedImages folderImage = new UploadedImages();
            folderImage.setPathToImages(orderDirPath);
            folderImage.setOrderId(order.getId());

            uploadedImagesRepository.save(folderImage);
            // Чистим временные файлы из сессии
            session.removeAttribute("tempFiles");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }
}
