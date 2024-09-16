package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.StatusOrders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusRepository extends JpaRepository<StatusOrders, Integer> {
    StatusOrders findByName(String name);

    List<StatusOrders> findByNameIn(List<String> names);
}
