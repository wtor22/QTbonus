package com.quartztop.bonus.user.controllers;

import com.quartztop.bonus.tokens.TokenCrudService;
import com.quartztop.bonus.tokens.TokenEntity;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/registration")
public class UserRegistrationController {

    private UserCrudService userCrudService;
    private TokenCrudService tokenCrudService;

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

        LocalDateTime expireDateToken = tokenEntity.getExpiryDate();
        if(LocalDateTime.now().isAfter(expireDateToken)) {
            return "tokens/token-outdated"; // имя страницы с ошибкой токен просрочен
        }

        Map<String,String> allUsers = new HashMap<>();
        allUsers.put("Борисов М.","borisov");
        allUsers.put("Ермолин Н.","ermolin");
        allUsers.put("Луцевич А.","lutsevich");
        allUsers.put("Сусликов А.","suslikov");
        allUsers.put("Закамская П. А.","zakamskaya");
        allUsers.put("Денисов Ф. Ф.","denisov");

        model.addAttribute("selectedManager",allUsers.keySet());
        model.addAttribute("formUrl","/registration/complete");
        model.addAttribute("token",token);

        return "complete-registration";
    }

    @PostMapping("/complete")
    public ResponseEntity<?> competeRegistration(@RequestBody UserDto userDto) {

        TokenEntity tokenEntity = tokenCrudService.getByToken(userDto.getToken())
                .orElseThrow(() -> new NoSuchElementException("токен не найден"));

        UserEntity userEntity = userCrudService.getUserById(
                tokenEntity.getUser().getId()).orElseThrow(() -> new NoSuchElementException("пользователь не найден"));

        userEntity.setPassword(userDto.getPassword());
        userEntity.setManager(userDto.getManager());
        userEntity.setAddress(userDto.getAddress());
        userEntity.setNameSalon(userDto.getNameSalon());

        userCrudService.updateUser(userEntity);
        tokenCrudService.updateStatus(tokenEntity.getToken());

        return ResponseEntity.ok().body(Map.of("redirectUrl", "/"));
    }


}
