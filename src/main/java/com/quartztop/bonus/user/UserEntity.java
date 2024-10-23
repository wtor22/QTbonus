package com.quartztop.bonus.user;

import com.quartztop.bonus.orders.TypeActivity;
import com.quartztop.bonus.user.roles.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


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
    private String nameSalon;
    private String innCompany;
    private String city;
    private String address;
    private String password;

    @ManyToOne
    @JoinColumn(name = "manager_id")  // Ссылка на менеджера
    private UserEntity manager;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @ManyToOne
    @JoinColumn(name = "type_activity")
    private TypeActivity typeActivity;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles roles;
}
