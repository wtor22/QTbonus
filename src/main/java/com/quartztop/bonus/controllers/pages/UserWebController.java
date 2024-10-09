package com.quartztop.bonus.controllers.pages;

import com.quartztop.bonus.crm.Invoices;
import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.orders.PaymentType;
import com.quartztop.bonus.orders.StatusOrders;
import com.quartztop.bonus.repositoriesBonus.OrderRepository;
import com.quartztop.bonus.repositoriesBonus.PaymentTypeRepository;
import com.quartztop.bonus.repositoriesBonus.StatusRepository;
import com.quartztop.bonus.repositoriesCrm.InvoicesRepository;
import com.quartztop.bonus.servises.MessageService;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import com.quartztop.bonus.repositoriesBonus.RolesRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/")
@AllArgsConstructor
public class UserWebController {

    private UserCrudService userCrudService;
    private MessageService messageService;
    private RolesRepository rolesRepository;
    private InvoicesRepository invoicesRepository;
    private OrderRepository orderRepository;
    private StatusRepository statusRepository;

    @GetMapping()
    public String getStart(Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            String userEmail = authentication.getName(); // Получаем имя пользователя
            UserEntity user = userCrudService.findByEmail(userEmail);
            String username = user.getFio();
            Roles userRole = user.getRoles();
            String welcomeMessage = messageService.getWelcomeMessage();
            model.addAttribute("username", username); // Передаем имя пользователя в модель
            model.addAttribute("userRole", userRole);
            model.addAttribute("nameRole",userRole.getNameRole());
            model.addAttribute("welcome",welcomeMessage);

            if(userRole.getRole().equals("ROLE_SUPER_ADMIN")) {
                model.addAttribute("listUsers", userCrudService.getAllUsers());
                model.addAttribute("listRoles", rolesRepository.findAllExceptRole("ROLE_SUPER_ADMIN"));

                List<Invoices> invoicesList = invoicesRepository.findAll();
                List<Order> ordersList = orderRepository.findAll();
                List<StatusOrders> statusOrdersList = statusRepository.findAll();

                model.addAttribute("ordersList", ordersList);
                model.addAttribute("invoicesList", invoicesList);
                model.addAttribute("statusList",statusOrdersList);
            }
            if(userRole.getRole().equals("ROLE_USER")) {

                model.addAttribute("formCreateOrder","/order/create");
            }
        } else {
            model.addAttribute("username", "Guest"); // Передаем имя пользователя в модель
        }

        model.addAttribute("formUrl","/api/user");
        model.addAttribute("formInputUrl","/login");
        return "index";
    }

    @PostMapping("/")
    public String handlePostRequest(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "index"; // Отображаем главную страницу с сообщением об ошибке
    }
}
