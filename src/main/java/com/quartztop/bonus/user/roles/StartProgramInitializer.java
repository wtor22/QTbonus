package com.quartztop.bonus.user.roles;

import com.quartztop.bonus.orders.PaymentType;
import com.quartztop.bonus.orders.StatusOrders;
import com.quartztop.bonus.repositoriesBonus.PaymentTypeRepository;
import com.quartztop.bonus.repositoriesBonus.RolesRepository;
import com.quartztop.bonus.repositoriesBonus.StatusRepository;
import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.repositoriesBonus.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class StartProgramInitializer implements CommandLineRunner {

    private RolesRepository rolesRepository;
    private UserRepository userRepository;
    private StatusRepository statusRepository;
    private PaymentTypeRepository paymentTypeRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Override
    public void run(String... args) throws Exception {

        List<String> listStatusNames = Arrays.asList("Создан","В обработке");

        List<StatusOrders> statusOrdersList = statusRepository.findByNameIn(listStatusNames);

        Set<String> namesSet = statusOrdersList.stream().map(StatusOrders::getName).collect(Collectors.toSet());

        List<StatusOrders> newStatusOrdersList = new ArrayList<>();

        // Проверяем, существуют ли статусы "Создан", "В обработке"
        if (!namesSet.contains("Создан")) {
            StatusOrders statusOrders = new StatusOrders();
            statusOrders.setName("Создан");
            statusOrders.setColor("bg-info");
            newStatusOrdersList.add(statusOrders);
        }
        if (!namesSet.contains("В обработке")) {
            StatusOrders statusOrders = new StatusOrders();
            statusOrders.setName("В обработке");
            statusOrders.setColor("bg-primary");
            newStatusOrdersList.add(statusOrders);
        }
        // Сохраняю новые статусы в бд
        statusRepository.saveAll(newStatusOrdersList);


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
            user.setFio("Вторушин");
            user.setPhone("00000000");
            user.setEmail("slavkontrakt@hotmail.com");

            //шифрую пароль
            user.setPassword(passwordEncoder.encode("12345678"));
            userRepository.save(user);
        }

        List<PaymentType> listNewPaymentType = new ArrayList<>();

        if(paymentTypeRepository.findByName("На Телефон") == null) {
            PaymentType paymentType = new PaymentType(1,"На Телефон");
            listNewPaymentType.add(paymentType);
        }
        if(paymentTypeRepository.findByName("На Телефон") == null) {
            PaymentType paymentType = new PaymentType(2,"На Банковский Счёт");
            listNewPaymentType.add(paymentType);
        }
        paymentTypeRepository.saveAll(listNewPaymentType);
    }
}