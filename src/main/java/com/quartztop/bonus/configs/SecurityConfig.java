package com.quartztop.bonus.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Отключаем CSRF для тестирования (не рекомендуется для продакшн)
                .authorizeHttpRequests((requests) -> requests
                        .anyRequest().permitAll()  // Разрешаем все запросы без авторизации
                )
                .formLogin((form) -> form
                        .loginPage("/login") // URL кастомной страницы входа
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll); // Разрешаем всем доступ к странице выхода
        return http.build();
    }
}
