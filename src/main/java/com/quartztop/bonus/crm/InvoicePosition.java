package com.quartztop.bonus.crm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "invoice_out_positions")
public class InvoicePosition {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "external_invoice_id")
    private String externalInvoiceId;

    @Column(name = "external_product_id")
    private String externalProductId;
    private double quantity;
    private double price;
    private double discount;
    private double vat;

    @Column(name = "product_type")
    private String productType; // Тип товара

//    @ManyToOne
//    @JoinColumn(name = "external_id")
//    private DemandEntity demand;
}
