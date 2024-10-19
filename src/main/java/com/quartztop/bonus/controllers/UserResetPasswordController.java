package com.quartztop.bonus.controllers;

import com.quartztop.bonus.servises.EmailService;
import com.quartztop.bonus.servises.MessageService;
import com.quartztop.bonus.tokens.TokenCrudService;
import com.quartztop.bonus.tokens.TokenEntity;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/password-reset")
public class UserResetPasswordController {

    private final UserCrudService userCrudService;
    private final TokenCrudService tokenCrudService;
    private MessageService messageService;
    private EmailService emailService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping
    public String getTokenToReset(@RequestParam String token, Model model) {

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

        model.addAttribute("userEmail", email);
        model.addAttribute("formPassResetUrl", "/password-reset");
        model.addAttribute("resetToken", token);

        return "password-reset";
    }

    @PostMapping("/get-link")
    public ResponseEntity<?> getLinkToReset(@RequestBody Map<String, String> request) throws MessagingException {

        String email = request.get("email");
        UserEntity userEntity = userCrudService.findByEmail(email).orElseThrow(() -> new RuntimeException("Пользователь с email " + email + " не найден"));

        senderLinkToCreatePassword(UserCrudService.mapToDto(userEntity));

        return ResponseEntity.ok().body("Ok");
    }

    @PostMapping
    public ResponseEntity<?> competeResetToken(@RequestBody Map<String, String> request) throws MessagingException {
//@RequestParam("token") String token, @RequestParam String newPassword

        String email = request.get("email");
        UserEntity userEntity = userCrudService.findByEmail(email).orElseThrow(() -> new RuntimeException("Пользователь с email " + email + " не найден"));

        String token = request.get("token");
        TokenEntity tokenEntity;
        // Сверяю с базой пришедший токен
        try {
            // Логика когда токен есть в базе
            tokenEntity = tokenCrudService.getByToken(token)
                    .orElseThrow(() -> new NoSuchElementException("токен не найден"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(400).body("tokens/token-not-found"); // имя страницы с ошибкой токен не найден
        }
        if(tokenEntity.isClosed()) {
            return ResponseEntity.status(410).body("tokens/token-not-found");
        }
        String newPassword = request.get("password");
        String encodedPassword = passwordEncoder.encode(newPassword);
        userEntity.setPassword(encodedPassword);

        userCrudService.updateUser(userEntity);
        tokenCrudService.updateStatus(tokenEntity.getToken());

        return ResponseEntity.ok().body("Ok");

    }

    private void senderLinkToCreatePassword(UserDto userDto) throws MessagingException {

        // Генерация токена для создания пароля
        String token = UUID.randomUUID().toString();
        tokenCrudService.create(token,userDto);
        // Формирование ссылки для создания пароля
        String passwordResetLink = messageService.getHost()
                + messageService.getPointResetPassword() + "?token=" + token;
        // Отправка письма пользователю
        String to = userDto.getEmail();
        String text = messageService.getTextResetPassword() + " " + passwordResetLink;
        emailService.sendEmail(to, text);
    }
}
