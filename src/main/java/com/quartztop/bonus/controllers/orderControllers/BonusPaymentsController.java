package com.quartztop.bonus.controllers.orderControllers;

import com.quartztop.bonus.orders.BonusPayments;
import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.user.UserCrudService;
import com.quartztop.bonus.user.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
@AllArgsConstructor
@Slf4j
public class BonusPaymentsController {

    private UserCrudService userCrudService;

    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestBody BonusPayments bonusPayment) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserEntity user = userCrudService
                .findByEmail(authentication.getName()).orElseThrow();


        bonusPayment.setUserId(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }
}
