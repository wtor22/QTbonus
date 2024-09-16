package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTypeRepository extends JpaRepository<PaymentType, Integer> {
    PaymentType findByName(String name);
}
