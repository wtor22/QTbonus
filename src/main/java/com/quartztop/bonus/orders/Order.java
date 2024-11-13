package com.quartztop.bonus.orders;


import com.quartztop.bonus.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    String type;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id")
    private StatusOrders status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bonus_value_id")
    BonusValue bonusValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_type_id")
    private PaymentType paymentType;

    @Column(name="sum_by_product")
    private double sumByProduct;

    private double sum; // Сумма выплаты

    @Column(name = "invoice_number")
    private String invoiceNumber; // Номер счета в Мой Склад

    @Column(name = "invoice_external_id")
    private String invoiceExternalId;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate; // Дата создания счета

    @Column(name = "product_external_id")
    private String productExternalId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_qtty")
    private double productQuantity;

    @Column(name = "data_payment")
    String dataPayment;

}
