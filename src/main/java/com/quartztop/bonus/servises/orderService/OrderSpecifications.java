package com.quartztop.bonus.servises.orderService;

import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.orders.StatusOrders;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
@Slf4j
public class OrderSpecifications {


    public static Specification<Order> hasDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return null; // Не добавляем фильтр, если обе даты отсутствуют
            }

            // Пример: поле createDate — это дата создания заказа
            Path<LocalDate> orderDatePath = root.get("createDate");

            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(orderDatePath, startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(orderDatePath, startDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(orderDatePath, endDate);
            }
        };
    }

    public static Specification<Order> hasFio(String fio) {
        return (root, query, criteriaBuilder) -> {
            if (fio == null) return null;
            // Создаем join с таблицей UserEntity
            Join<Object, Object> userEntityJoin = root.join("userEntity");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(userEntityJoin.get("fio")),
                    "%" + fio.toLowerCase() + "%" // Шаблон в нижнем регистре
            );
        };
    }
    public static Specification<Order> hasInvoice(String invoiceNumber) {
        return (root, query, criteriaBuilder) -> {
            if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) return null;
            return criteriaBuilder.like(root.get("invoiceNumber"),"%" + invoiceNumber + "%");
        };
    }

    public static Specification<Order> hasManager(String managerFio) {
        return (root, query, criteriaBuilder) -> {
            if (managerFio == null) return null;
            // Создаем join с таблицей UserEntity
            Join<Object, Object> userEntityJoin = root.join("userEntity");
            // Создаем дополнительный join с полем manager
            Join<Object, Object> managerJoin = userEntityJoin.join("manager");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(managerJoin.get("fio")),
                    "%" + managerFio.toLowerCase() + "%");
        };
    }

    public static Specification<Order> hasType(String type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null || type.isEmpty()) return null;
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<Order> hasStatus(Integer id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) return null;
            Join<Object,Object> statusJoin = root.join("status");
            return criteriaBuilder.equal(statusJoin.get("id"), id);
        };
    }
}
