package com.quartztop.bonus.orders;

import com.quartztop.bonus.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "bonus_payment")
public class BonusPayments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "date_payment")
    private LocalDateTime datePayment;

    private double sum;

    @Column(name = "comment_to_payment")
    private String commentToPayment;

    @ManyToOne
    private Order order;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity userId;
}
