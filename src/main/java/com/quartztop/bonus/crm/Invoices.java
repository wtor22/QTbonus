package com.quartztop.bonus.crm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "invoice_out")
public class Invoices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_external", unique = true)
    private String externalId; // id счёта в Мой Склад
    private String name; // Номер счета в Мой Склад
    private LocalDateTime moment; // Дата создания счета
    private String applicable; // Флаг. Проведён ли документ в МС
    private double sum; // Сумма отгрузки

    @ManyToOne
    @JoinColumn(name = "agent")
    private Agent agent;
}
