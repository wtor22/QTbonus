package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByType(String type, Pageable pageable);
    List<Order> findByType(String type, Sort sort);
    List<Order> getOrdersByType(String type);
    List<Order> getOrdersByUserEntity(UserEntity userEntity);

    Order getOrderByUserEntityAndId(UserEntity userEntity, Integer id);

    List<Order> getOrdersByUserEntityAndType(UserEntity userEntity, String type);

    List<Order> getOrdersByUserEntityInAndType(List<UserEntity> userEntities, String type);

    List<Order> getOrdersByInvoiceExternalIdAndProductExternalId(String invoiceExternalId, String productExternalId);
}
