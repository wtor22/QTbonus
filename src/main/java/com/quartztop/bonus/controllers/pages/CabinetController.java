package com.quartztop.bonus.controllers.pages;

import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/cabinet")
@RequiredArgsConstructor
public class CabinetController {

    private final UserCrudService userCrudService;

    @GetMapping()
    public String getPage(Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            String userEmail = authentication.getName(); // Получаем имя пользователя
            UserEntity user = userCrudService.findByEmail(userEmail).orElseThrow();
            String username = user.getFio();
            Roles userRole = user.getRoles();
            model.addAttribute("user",user);
            model.addAttribute("username", username); // Передаем имя пользователя в модель
            model.addAttribute("userRole", userRole);
            model.addAttribute("nameRole", userRole.getNameRole());
        }
        return "cabinet";
    }
}
