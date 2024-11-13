package com.quartztop.bonus.crm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;

    @Column(name = "id_external", unique = true)
    private String externalId;

    private String article;
    private String name;
    private String description;

    @Column(name = "path_name")
    private String pathName;

    @Column(name = "type_product")
    private String typeProduct;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
