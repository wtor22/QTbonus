package com.quartztop.bonus.crm;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "category_product")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "id_external", unique = true)
    private String externalId;
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
