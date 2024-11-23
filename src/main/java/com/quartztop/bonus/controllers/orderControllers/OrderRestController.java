package com.quartztop.bonus.controllers.orderControllers;

import com.quartztop.bonus.crm.Product;
import com.quartztop.bonus.orders.*;
import com.quartztop.bonus.repositoriesBonus.BonusValueRepositories;
import com.quartztop.bonus.repositoriesBonus.OrderRepository;
import com.quartztop.bonus.repositoriesBonus.StatusRepository;
import com.quartztop.bonus.repositoriesBonus.UploadedImagesRepository;
import com.quartztop.bonus.repositoriesCrm.ProductRepositories;
import com.quartztop.bonus.servises.FileService;
import com.quartztop.bonus.servises.orderService.OrderService;
import com.quartztop.bonus.servises.orderService.OrderSpecifications;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import java.util.HashMap;
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
    private final OrderService orderService;
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
        List<OrderDto> userOrders = orders.stream().map(OrderService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }

    // Получить ордера с фильтром
    @GetMapping("/get-order-filter")
    public List<Order> getOrderByFilter(
            @RequestParam(required = false) String fio
    ) {
        List<Order> list = orderService.searchOrders(fio);
        log.info("LIST SIZE " + list.size());
        return list;
    }

    // Получить ордер по id
    @GetMapping("/get-order")
    public ResponseEntity<?> getUserOrderById(@RequestParam("id") int orderId, Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен");
        }

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
            OrderDto orderDto = OrderService.mapOrderToDto(order);

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

        OrderDto orderDto = OrderService.mapOrderToDto(order);
        return ResponseEntity.ok(orderDto);
    }

    // Получить ордер Для Менеджера по id
    @GetMapping("/get-order-by-manager")
    public ResponseEntity<?> getOrderForManagerById(@RequestParam("id") int orderId, Principal principal) {

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

        OrderDto orderDto = OrderService.mapOrderToDto(order);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(Principal principal,
            @RequestParam(defaultValue = "bonus") String type,
            @RequestParam(defaultValue = "createDate") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending,
            @RequestParam(required = false) String fio,
            @RequestParam(required = false) String managerFio,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен");
        }
        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();
        if(sortBy.equals("user")) sortBy = "userEntity.fio";
        if(sortBy.equals("manager")) sortBy = "userEntity.manager.fio";

        // Создаем спецификацию для фильтрации
        Specification<Order> spec = Specification.where(OrderSpecifications.hasFio(fio))
                .and(OrderSpecifications.hasType(type))
                .and(OrderSpecifications.hasManager(managerFio))
                .and(OrderSpecifications.hasStatus(statusId))
                .and(OrderSpecifications.hasInvoice(invoiceNumber))
                .and(OrderSpecifications.hasDateRange(startDate, endDate));

        List<Order> orders = orderService.getOrdersByTypeWithSort(spec,sortBy,ascending);

        List<OrderDto> userOrders = orders.stream()
                .map(OrderService::mapOrderToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userOrders);
    }

    // Получить список заказов бонусов
    @GetMapping("/get-orders")
    public ResponseEntity<List<OrderDto>> getUserOrders(Principal principal) {

        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();


        List<Order> orders = orderRepository.getOrdersByUserEntityAndType(user, "bonus");
        List<OrderDto> userOrders = orders.stream().map(OrderService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }
    // Получить список заказов бонусов по партнерам менеджера
    @GetMapping("/get-orders-by-manager")
    public ResponseEntity<List<OrderDto>> getManagerOrders(Principal principal) {

        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();

        List<UserEntity> usersByManagerList = userCrudService.getAllUsersEntityByManager(user);
        List<Order> orders = orderRepository.getOrdersByUserEntityInAndType(usersByManagerList,"bonus");
        List<OrderDto> userOrders = orders.stream().map(OrderService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }

    @GetMapping("/get-all-orders")
    public ResponseEntity<List<OrderDto>> getAllOrders(Principal principal) {

        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();
        if(!user.getRoles().getRole().equals("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Order> orders = orderRepository.getOrdersByType("bonus");
        List<OrderDto> ordersList = orders.stream().map(OrderService::mapOrderToDto)
                .toList();
        return ResponseEntity.ok(ordersList);
    }

    // Получить список акций Пользователя
    @GetMapping("/get-actions")
    public ResponseEntity<List<OrderDto>> getUserActions(Principal principal) {

        String username = principal.getName();
        UserEntity user = userCrudService.findByEmail(username).orElseThrow();

        List<Order> actions = orderRepository.getOrdersByUserEntityAndType(user, "action");
        List<OrderDto> userOrders = actions.stream().map(OrderService::mapOrderToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userOrders);
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<StatusOrders>> getAllStatus() {
        return ResponseEntity.ok(statusRepository.findAll());
    }

    @PostMapping("/set-status")
    public ResponseEntity<OrderDto> setStatusOrder(HttpServletRequest request, @RequestBody Order order) {

        Order orderExisting = orderRepository.findById(order.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        StatusOrders statusOrders =statusRepository.findById(order.getStatus().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        orderExisting.setStatus(statusOrders);
        Order updateOrder = orderRepository.save(orderExisting);

        OrderDto updateOrderDto = OrderService.mapOrderToDto(updateOrder);

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
