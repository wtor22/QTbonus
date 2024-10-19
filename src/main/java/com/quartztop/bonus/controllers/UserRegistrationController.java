package com.quartztop.bonus.controllers;

import com.quartztop.bonus.tokens.TokenCrudService;
import com.quartztop.bonus.tokens.TokenEntity;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.repositoriesBonus.RolesRepository;
import com.quartztop.bonus.user.roles.Roles;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/registration")
public class UserRegistrationController {

    private final UserCrudService userCrudService;
    private final TokenCrudService tokenCrudService;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping
    public String getToken(@RequestParam String token, Model model) {

        TokenEntity tokenEntity;
        // Сверяю с базой пришедший токен
        try {
            // Логика когда токен есть в базе
            tokenEntity = tokenCrudService.getByToken(token)
                    .orElseThrow(() -> new NoSuchElementException("токен не найден"));
        } catch (NoSuchElementException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "tokens/token-not-found"; // имя страницы с ошибкой токен не найден
        }

        if(tokenEntity.isClosed()) {
            return "tokens/token-not-found"; // имя страницы с ошибкой токен не найден
        }

        UserEntity userEntity = userCrudService.getUserById(
                tokenEntity.getUser().getId()).orElseThrow(() -> new NoSuchElementException("пользователь не найден"));

        String email = userEntity.getEmail();
        LocalDateTime expireDateToken = tokenEntity.getExpiryDate();
        if(LocalDateTime.now().isAfter(expireDateToken)) {
            return "tokens/token-outdated"; // имя страницы с ошибкой токен просрочен
        }

        Roles role = rolesRepository.getReferenceById(2);
        List<UserEntity> listManagers = userCrudService.getAllUsers(role);

        Map<String, Integer> allUsers = new HashMap<>();

        if(tokenEntity.getManager() == null) {
            allUsers.putAll(listManagers.stream()
                            .collect(Collectors
                                    .toMap(UserEntity::getFio, UserEntity::getId)));
        } else {
            UserEntity manager = userCrudService.getUserByFio(tokenEntity.getManager());
            allUsers.put(manager.getFio(),manager.getId());
        }

        allUsers.forEach((fio,id)-> log.info(fio + " " + id));

        model.addAttribute("selectedManager",allUsers.keySet());
        model.addAttribute("formUrl","/registration/complete");
        model.addAttribute("token",token);
        model.addAttribute("userEmail", email);

        return "complete-registration";
    }

    @PostMapping("/complete")
    public ResponseEntity<?> competeRegistration(@RequestBody UserDto userDto,  Model model) {

        log.info("USERDTO GET MANAGER " + userDto.getManager());
        TokenEntity tokenEntity = tokenCrudService.getByToken(userDto.getToken())
                .orElseThrow(() -> new NoSuchElementException("токен не найден"));

        UserEntity userEntity = userCrudService.getUserById(
                tokenEntity.getUser().getId()).orElseThrow(() -> new NoSuchElementException("пользователь не найден"));

        UserEntity manager = userCrudService.getUserByFio(userDto.getManager());

        log.info("MANAGER FIO " + manager.getFio() + " CITY " + userDto.getCity());
        //шифрую пароль
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userEntity.setPassword(encodedPassword);
        userEntity.setManager(manager);
        userEntity.setCity(userDto.getCity());
        userEntity.setAddress(userDto.getAddress());
        userEntity.setNameSalon(userDto.getNameSalon());
        userEntity.setCreateDate(LocalDateTime.now());

        userCrudService.updateUser(userEntity);
        tokenCrudService.updateStatus(tokenEntity.getToken());

        return ResponseEntity.ok().body(Map.of("redirectUrl", "/"));
    }

    @GetMapping("/token")
    public String registrationByToken(@RequestParam(name = "token", required = true) String token, Model model){

        TokenEntity tokenEntity = tokenCrudService.getByToken(token).orElseThrow();
        UserEntity manager = tokenEntity.getManagerId();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            String userEmail = authentication.getName(); // Получаем имя пользователя
            UserEntity user = userCrudService.findByEmail(userEmail).orElseThrow();
            String username = user.getFio();
            Roles userRole = user.getRoles();

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
