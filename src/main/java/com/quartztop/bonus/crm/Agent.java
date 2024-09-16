package com.quartztop.bonus.crm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "agent")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_external", unique = true)
    private String externalId; // id счёта в Мой Склад
    private String name; // Номер счета в Мой Склад
    private String inn;

}
