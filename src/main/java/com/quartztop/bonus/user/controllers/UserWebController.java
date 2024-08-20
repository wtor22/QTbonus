package com.quartztop.bonus.user.controllers;

import com.quartztop.bonus.servises.MessageService;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import com.quartztop.bonus.user.roles.RolesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.management.relation.Role;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class UserWebController {

    private final UserCrudService userCrudService;
    private final MessageService messageService;
    private final RolesRepository rolesRepository;

    public UserWebController(UserCrudService userCrudService, MessageService messageService, RolesRepository rolesRepository) {
        this.userCrudService = userCrudService;
        this.messageService = messageService;
        this.rolesRepository = rolesRepository;
    }

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
