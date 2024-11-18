package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.BonusPayments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BonusPaymentsRepository extends JpaRepository<BonusPayments, Integer> {
}
