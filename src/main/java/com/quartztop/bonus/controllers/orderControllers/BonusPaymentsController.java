package com.quartztop.bonus.controllers.orderControllers;

import com.quartztop.bonus.orders.BonusPayments;
import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.repositoriesBonus.BonusPaymentsRepository;
import com.quartztop.bonus.repositoriesBonus.OrderRepository;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
@AllArgsConstructor
@Slf4j
public class BonusPaymentsController {

    private UserCrudService userCrudService;
    private BonusPaymentsRepository bonusPaymentsRepository;
    private OrderRepository orderRepository;

    @PostMapping("/create")
    public ResponseEntity<BonusPayments> createPayment(@RequestBody BonusPayments bonusPayment) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserEntity user = userCrudService
                .findByEmail(authentication.getName()).orElseThrow();

        bonusPayment.setUserId(user);

        BonusPayments newBonusPayment = bonusPaymentsRepository.save(bonusPayment);

        return ResponseEntity.status(HttpStatus.CREATED).body(newBonusPayment);
    }

    @GetMapping("/get-payments")
    public ResponseEntity<List<BonusPayments>> getListPayments(@RequestParam int orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if(optionalOrder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<BonusPayments> bonusPaymentsList = bonusPaymentsRepository.findAllByOrder(optionalOrder.get());
        return ResponseEntity.status(HttpStatus.OK).body(bonusPaymentsList);
    }

}
