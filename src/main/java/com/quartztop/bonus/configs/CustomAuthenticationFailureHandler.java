package com.quartztop.bonus.configs;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // Устанавливаем сообщение об ошибке в атрибут запроса
        request.setAttribute("error", "Неверный логин или пароль");
        // Перенаправляем на главную страницу
        request.getRequestDispatcher("/").forward(request, response);
    }
}
