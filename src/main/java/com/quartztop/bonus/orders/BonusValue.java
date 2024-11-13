package com.quartztop.bonus.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@Table(name = "bonus_value")
public class BonusValue {

    @Id
    private int id;

    String name;
    String description;
    double value;

    @Column(name = "start_date")
    private LocalDate startDate; // Дата начала

    @Column(name = "end_date")
    private LocalDate endDate; // Дата начала

    public BonusValue() {}

    public BonusValue(int id, String name, String description, double value, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
