package com.quartztop.bonus.servises.orderService;

import com.quartztop.bonus.orders.Order;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {

    // Фильтрация по имени категории
    public static Specification<Order> hasFio(String fio) {
        return (root, query, criteriaBuilder) -> {
            if (fio == null) return null;
            // Создаем join с таблицей UserEntity
            Join<Object, Object> userEntityJoin = root.join("userEntity");
            return criteriaBuilder.like(userEntityJoin.get("fio"),"%" + fio + "%");
        };
    }

    public static Specification<Order> hasManager(String managerFio) {
        return (root, query, criteriaBuilder) -> {
            if (managerFio == null) return null;
            // Создаем join с таблицей UserEntity
            Join<Object, Object> userEntityJoin = root.join("userEntity");
            // Создаем дополнительный join с полем manager
            Join<Object, Object> managerJoin = userEntityJoin.join("manager");
            return criteriaBuilder.like(managerJoin.get("fio"),"%" + managerFio + "%");
        };
    }

    public static Specification<Order> hasType(String type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null || type.isEmpty()) return null;
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }
}
