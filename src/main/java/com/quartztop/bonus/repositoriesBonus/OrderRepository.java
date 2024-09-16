package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> getOrdersByUserEntity(UserEntity userEntity);

    List<Order> getOrdersByInvoiceExternalId(String invoiceExternalId);
}
