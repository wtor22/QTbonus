package com.quartztop.bonus.user.roles;

import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAndRolesInitializer implements CommandLineRunner {

    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Override
    public void run(String... args) throws Exception {

        // Проверяем, существует ли роль "ROLE_USER"
        if (rolesRepository.findByRole("ROLE_USER") == null) {
            // Если роли нет, создаем и сохраняем её
            Roles userRole = new Roles();
            userRole.setRole("ROLE_USER");
            userRole.setNameRole("Пользователь");
            rolesRepository.save(userRole);
        }

        // Проверяем, существует ли роль "ROLE_ADMIN"
        if (rolesRepository.findByRole("ROLE_ADMIN") == null) {
            // Если роли нет, создаем и сохраняем её
            Roles adminRole = new Roles();
            adminRole.setRole("ROLE_ADMIN");
            adminRole.setNameRole("Администратор");
            rolesRepository.save(adminRole);
        }

        // Проверяем, существует ли роль "ROLE_SUPER_ADMIN"
        if (rolesRepository.findByRole("ROLE_SUPER_ADMIN") == null) {
            // Если роли нет, создаем и сохраняем её
            Roles adminRole = new Roles();
            adminRole.setRole("ROLE_SUPER_ADMIN");
            adminRole.setNameRole("Супер Админ");
            rolesRepository.save(adminRole);
            UserEntity user = new UserEntity();
            user.setRoles(adminRole);
            user.setFio("");
            user.setPhone("00000000");
            user.setEmail("slavkontrakt@hotmail.com");

            //шифрую пароль
            user.setPassword(passwordEncoder.encode("12345678"));
            userRepository.save(user);
        }



    }
}
