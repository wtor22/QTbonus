package com.quartztop.bonus.orders;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "payment_type")
public class PaymentType {
    @Id
    private int id;

    private String name;

    public PaymentType() {
    }
    public PaymentType(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
