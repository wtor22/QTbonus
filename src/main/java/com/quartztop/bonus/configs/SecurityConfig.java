package com.quartztop.bonus.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler1) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler1;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(withDefaults())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/","/registration/**","/registration","/403","/error/**","/fonts/**","/images/**","/css/**", "/js/**", "/api/**",
                               "/order/**", "/login", "/first-registration").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        //.loginPage("/login")
                        .loginProcessingUrl("/login")
                        //.defaultSuccessUrl("/")
                        .failureHandler(new CustomAuthenticationFailureHandler())
                        .permitAll()
                        .successHandler(customAuthenticationSuccessHandler)
                )
                .userDetailsService(customUserDetailsService)
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")  // URL для logout
                                .logoutSuccessUrl("/")  // URL после успешного logout
                                .invalidateHttpSession(true)  // Инвалидируем сессию
                                .deleteCookies("JSESSIONID")  // Удаляем cookies
                                .permitAll()
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    // Редирект на 403 для неавторизованных пользователей
                                    response.sendRedirect("/403");
                                })
                                .accessDeniedPage("/403") // для авторизованных, но без прав
                );
        return http.build();
    }
}
