package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.BonusPayments;
import com.quartztop.bonus.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BonusPaymentsRepository extends JpaRepository<BonusPayments, Integer> {

    List<BonusPayments> findAllByOrder(Order order);
}
