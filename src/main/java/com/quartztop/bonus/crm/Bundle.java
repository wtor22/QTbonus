package com.quartztop.bonus.crm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bundle")
@Getter
@Setter
public class Bundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "product_parent_id")
    private Product productParent;

    private double quantity;

    @Column(name = "id_external", unique = true)
    private String externalId;
}
