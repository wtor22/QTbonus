package com.quartztop.bonus.orders;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "status_orders")
public class StatusOrders {

    @Id
    private int id;
    String color;
    String name;
}
