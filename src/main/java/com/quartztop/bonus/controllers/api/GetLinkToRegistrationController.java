package com.quartztop.bonus.controllers.api;

import com.quartztop.bonus.servises.MessageService;
import com.quartztop.bonus.tokens.TokenCrudService;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/api/get-link")
@AllArgsConstructor
public class GetLinkToRegistrationController {

    private final UserCrudService userCrudService;
    private final TokenCrudService tokenCrudService;
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> getLink(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            String userEmail = authentication.getName(); // Получаем имя пользователя
            UserEntity user = userCrudService.findByEmail(userEmail).orElseThrow();
            Roles userRole = user.getRoles();

            if(userRole.getRole().equals("ROLE_MANAGER")) {

                LocalDateTime expiryDate = LocalDate.now().atTime(LocalTime.MAX);

                // Генерация токена для создания ссылки
                String token = UUID.randomUUID().toString();
                tokenCrudService.createTokenToLinkRegistry(token,user, expiryDate);
                // Формирование ссылки
                String registeredLink =messageService.getHost()
                        + messageService.getPointCreateUserByLinkManager() + token;

                response.put("registeredLink", registeredLink);

                LocalDateTime expiryLimitDate = expiryDate.minusMinutes(1);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM HH:mm");
                String formatteDate = expiryLimitDate.format(formatter);
                String limitLink = "Ссылка действительна до " + formatteDate;

                response.put("limitLink", limitLink);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
        }
        response.put("registeredLink", "Не вижу кто ты");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


}
