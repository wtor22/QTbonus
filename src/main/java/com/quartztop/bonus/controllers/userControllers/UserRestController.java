package com.quartztop.bonus.controllers.userControllers;

import com.quartztop.bonus.DuplicateResourceException;
import com.quartztop.bonus.ResourceNotFoundException;
import com.quartztop.bonus.orders.TypeActivity;
import com.quartztop.bonus.repositoriesBonus.TypeActivityRepositories;
import com.quartztop.bonus.repositoriesCrm.AgentRepository;
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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
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
    private TypeActivityRepositories typeActivityRepositories;
    private AgentRepository agentRepository;

    @PostMapping("/update")
    public ResponseEntity<?> update(Principal principal, @RequestBody UserDto userDto) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        // Проверка существует ли контрагент с таким ИНН
        if(agentRepository.findAllByInn(userDto.getInnCompany()).isEmpty()) {
            String response = "Контрагент с ИНН " + userDto.getInnCompany() + " не найден!";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message",response));
        }

        // Получаем email пользователя из Principal
        String email = principal.getName();

        // Получаем сущность UserEntity из базы по email
        UserEntity user = userCrudService.findUserByEmail(email);

        try {
            // Обновляем данные пользователя
            userCrudService.updateUser(user.getId(), userDto);

            // Возвращаем успешный ответ
            return ResponseEntity.ok(Map.of("message" ,"User updated successfully"));
        } catch (ResourceNotFoundException e) {
            // Обрабатываем ситуацию, если пользователь не найден
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","User not found"));
        } catch (DuplicateResourceException e) {
            // Обрабатываем ошибку уникальности email или телефона
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            // Обрабатываем другие ошибки
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","An error occurred while updating user"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) throws MessagingException {

        log.info("USER INN COMPANY ");
        log.info("USER INN COMPANY " + userDto.getInnCompany());
        String userEmail = userDto.getEmail();
        UserEntity existingUser = userCrudService.findUserByEmail(userEmail);

        if (existingUser !=null && existingUser.getPassword() == null) {
            log.info("TYPE ACTIVITY " + userDto.getTypeActivity());
        // Если пароля нет, обновляем существующую запись
            existingUser.setPhone(userDto.getPhone());
            existingUser.setFio(userDto.getFio());
            Optional<TypeActivity> typeActivityOptional = typeActivityRepositories.findById(userDto.getTypeActivity());
            if(typeActivityOptional.isPresent()) {
                TypeActivity typeActivity = typeActivityOptional.get();
                existingUser.setTypeActivity(typeActivity);
            }

            // обновляю пользователя
            userCrudService.updateUser(existingUser);
            userDto.setId(existingUser.getId());
            // Отправляю ссылку
            senderLinkToCreatePassword(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        }
        if(existingUser == null) {
            // Если юзера нет то создаем
            log.info("СОЗДАЮ ЮЗЕРА");
            UserDto createdUser = userCrudService.create(userDto).orElseThrow();
            log.info("ОТПРАВЛЯЮ ЛИНК");
            senderLinkToCreatePassword(createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        }
        Map<String,String> response = new HashMap<>();
        response.put("message", messageService.getErrorMessageEmail());
        return ResponseEntity.status(400).body(response);
    }

    @GetMapping
    public ResponseEntity<UserDto> getUser(@RequestParam Integer id) {
        UserDto user = userCrudService.getUser(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    private void senderLinkToCreatePassword(UserDto userDto) throws MessagingException {

        // Генерация токена для создания пароля
        String token = UUID.randomUUID().toString();
        tokenCrudService.create(token,userDto);
        // Формирование ссылки для создания пароля
        String passwordResetLink = messageService.getHost()
                + messageService.getPointCreateUser() + "?token=" + token;
        // Отправка письма пользователю
        String to = userDto.getEmail();
        String text = messageService.getTextCreateAccount() + " " + passwordResetLink;
        String htmlText = "<p>" + messageService.getTextCreateAccount() + "</p>" +
                "<a href=\"" + passwordResetLink + "\">" + passwordResetLink + "</a>";
        emailService.sendEmail(to, text,htmlText);
    }
}
