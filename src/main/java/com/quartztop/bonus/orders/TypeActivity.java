package com.quartztop.bonus.orders;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "type_activity")
public class TypeActivity {

    @Id
    private int id;

    private String name;

    public TypeActivity() {
    }
    public TypeActivity(int id, String name) {
        this.id = id;
        this.name = name;
    }


}
