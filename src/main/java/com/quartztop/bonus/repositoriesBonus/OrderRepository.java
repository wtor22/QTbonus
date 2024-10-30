package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> getOrdersByUserEntity(UserEntity userEntity);

    Order getOrderByUserEntityAndId(UserEntity userEntity, Integer id);

    List<Order> getOrdersByUserEntityAndType(UserEntity userEntity, String type);

    List<Order> getOrdersByInvoiceExternalId(String invoiceExternalId);
}
