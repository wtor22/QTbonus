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
    private String externalId;
    private String name;
    private String inn;
    @Column(name = "legal_title")
    private String fullName;
}
