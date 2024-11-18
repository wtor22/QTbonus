package com.quartztop.bonus.orders;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BonusValueDto {

    private int id;
    private String name;
    private String description;
    private double value;
    private LocalDate startDate; // Дата начала
    private LocalDate endDate; // Дата начала
}
