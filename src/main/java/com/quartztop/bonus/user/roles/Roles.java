package com.quartztop.bonus.user.roles;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Roles {

    @Id
    int id;
    String role;
    String nameRole;
}
