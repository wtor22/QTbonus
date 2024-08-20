package com.quartztop.bonus.user;

import com.quartztop.bonus.user.roles.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(name = "e_mail", unique = true)
    private String email;
    private String fio;
    @Column(name = "phone", unique = true)
    private String phone;
    private String manager;
    private String nameSalon;
    private String address;
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles roles;
}
