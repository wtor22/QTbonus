package com.quartztop.bonus.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Setter
@Getter
public class OrderDto {

    private int id;
    private String createDate;
    private String statusName;
    private String statusColor;
    private String paymentType;
    private double sum;
    private String invoiceNumber;
    private String invoiceExternalId;
    private LocalDate invoiceDate; // Дата создания счета
    private String productExternalId;
    private String productName;
    private double productQuantity;
}
