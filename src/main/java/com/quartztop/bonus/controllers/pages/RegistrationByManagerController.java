package com.quartztop.bonus.controllers.pages;

import com.quartztop.bonus.crm.Invoices;
import com.quartztop.bonus.orders.StatusOrders;
import com.quartztop.bonus.repositoriesBonus.RolesRepository;
import com.quartztop.bonus.repositoriesBonus.StatusRepository;
import com.quartztop.bonus.repositoriesCrm.InvoicesRepository;
import com.quartztop.bonus.servises.MessageService;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/first-registration")
@AllArgsConstructor
public class RegistrationByManagerController {

    private UserCrudService userCrudService;
    private MessageService messageService;

    @GetMapping()
    public String getStart(@RequestParam(name = "id", required = false) Integer managerId, Model model){

        UserEntity manager = userCrudService.getUserById(managerId).orElseThrow();

        log.info("MANAGER " + manager.getFio());

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
            model.addAttribute("welcome","Вы уже зарегистрированы");

            if(userRole.getRole().equals("ROLE_USER")) {

                model.addAttribute("formCreateOrder","/order/create");
            }
        } else {
            model.addAttribute("username", "Guest"); // Передаем имя пользователя в модель
            model.addAttribute("manager", manager.getFio());
        }

        model.addAttribute("formUrl","/api/user");

        return "first-registration";
    }
}
