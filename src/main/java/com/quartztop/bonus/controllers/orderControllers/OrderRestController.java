package com.quartztop.bonus.controllers.orderControllers;

import com.quartztop.bonus.crm.Product;
import com.quartztop.bonus.orders.*;
import com.quartztop.bonus.repositoriesBonus.BonusValueRepositories;
import com.quartztop.bonus.repositoriesBonus.OrderRepository;
import com.quartztop.bonus.repositoriesBonus.StatusRepository;
import com.quartztop.bonus.repositoriesBonus.UploadedImagesRepository;
import com.quartztop.bonus.repositoriesCrm.ProductRepositories;
import com.quartztop.bonus.servises.FileService;
import com.quartztop.bonus.servises.orderService.OrderDtoService;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/order")
public class OrderRestController {

    private final OrderRepository orderRepository;
    private final ProductRepositories productRepositories;
    private final UploadedImagesRepository uploadedImagesRepository;
    private final UserCrudService userCrudService;
    private final StatusRepository statusRepository;
    private final FileService fileService;
    private final BonusValueRepositories bonusValueRepositories;

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

        Roles roles = user.getRoles();
        if(roles.getRole().equals("ROLE_ADMIN") || roles.getRole().equals("ROLE_SUPER_ADMIN") ) {

            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if(optionalOrder.isEmpty()) {
                String message = "Ордер не найден";
                Map<String, String> response = Map.of("message", message);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Order order = optionalOrder.get();
            OrderDto orderDto = OrderDtoService.mapOrderToDto(order);

            UserDto client = userCrudService.getUser(order.getUserEntity().getId());
            orderDto.setUserDto(client);
            return ResponseEntity.ok(orderDto);
        }

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

    @PostMapping("/set-status")
    public ResponseEntity<OrderDto> setStatusOrder(HttpServletRequest request, @RequestBody Order order) {

        Order orderExisting = orderRepository.findById(order.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        StatusOrders statusOrders =statusRepository.findById(order.getStatus().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        orderExisting.setStatus(statusOrders);
        Order updateOrder = orderRepository.save(orderExisting);

        OrderDto updateOrderDto = OrderDtoService.mapOrderToDto(updateOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(updateOrderDto);
    }
    @PostMapping("/create")
    public ResponseEntity<String> createOrder(HttpServletRequest request, @RequestBody Order order) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserEntity user = userCrudService
                .findByEmail(authentication.getName()).orElseThrow(); // authentication.getName() это адрес email

        Product product = productRepositories.findByExternalId(order.getProductExternalId());

        LocalDate dateInvoice = order.getInvoiceDate();

        BonusValue bonusValue = getDiscount(product, user, dateInvoice);

        double discount = bonusValue.getValue();

        double summByProduct = order.getSumByInvoice();

        int sumBonus = (int) (summByProduct * discount / 100);

        order.setSum(sumBonus);
        order.setBonusValue(bonusValue);

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

    public BonusValue getDiscount(Product product, UserEntity user, LocalDate dateInvoice) {

        LocalDate startDate = LocalDate.of(2024,11,1);
        LocalDate endDate = LocalDate.of(2024,12,31);

        // Если в промежутке дат - то устанавливаем скидки
        if((dateInvoice.isEqual(startDate) || dateInvoice.isAfter(startDate)) &&
        (dateInvoice.isEqual(endDate) || dateInvoice.isBefore(endDate))) {

            if(product.getCategory().getName().equals("Stratos")) {
                return bonusValueRepositories.findById(3).orElseThrow();
            }
            if(user.getManager().getEmail().equals("p.zakam@quartztop.ru") && product.getCategory().getName().equals("Belenco")){
                return bonusValueRepositories.findById(2).orElseThrow();
            }
        }
        // Возвращаем стандартный бонус
        return bonusValueRepositories.findById(1).orElseThrow();
    }
}
