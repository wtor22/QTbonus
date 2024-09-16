package com.quartztop.bonus.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        // Устанавливаем статус 401 (Unauthorized)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Формируем JSON ответ
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Отправляем JSON с сообщением об ошибке
        response.getWriter().write("{\"error\": \" Неверный Email или Пароль \"}");
    }
}
