package com.quartztop.bonus.user.controllers;

import com.quartztop.bonus.DuplicateResourceException;
import com.quartztop.bonus.servises.EmailService;
import com.quartztop.bonus.servises.MessageService;
import com.quartztop.bonus.tokens.TokenCrudService;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserDto;
import com.quartztop.bonus.user.UserEntity;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/user")
public class UserRestController {

    private UserCrudService userCrudService;
    private EmailService emailService;
    private TokenCrudService tokenCrudService;
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) throws MessagingException {

        UserEntity existingUser = userCrudService.findByEmail(userDto.getEmail());

        if (existingUser != null && existingUser.getPassword().isEmpty()) {

                // Если пароля нет, обновляем существующую запись
                existingUser.setPhone(userDto.getPhone());
                existingUser.setFio(userDto.getFio());

                // обновляю пользователя
                userCrudService.updateUser(existingUser);

                userDto.setId(existingUser.getId());
                // Отправляю ссылку
                senderLinkToCreatePassword(userDto);

                return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        }


        // Если юзера нет то создаем
        UserDto createdUser = userCrudService.create(userDto).orElseThrow();
        senderLinkToCreatePassword(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }



    @GetMapping
    public ResponseEntity<Optional<UserDto>> getUser(@RequestParam Integer id) {
        Optional<UserDto> user = userCrudService.getUser(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    private void senderLinkToCreatePassword(UserDto userDto) throws MessagingException {

        // Генерация токена для создания пароля
        String token = UUID.randomUUID().toString();
        tokenCrudService.create(token,userDto);
        // Формирование ссылки для создания пароля
        String passwordResetLink = "http://" + messageService.getHost()
                + messageService.getPointCreateUser() + "?token=" + token;
        // Отправка письма пользователю
        String to = userDto.getEmail();
        String text = messageService.getTextCreateAccount() + " " + passwordResetLink;
        emailService.sendEmail(to, text);
    }

}
