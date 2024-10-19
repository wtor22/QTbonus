package com.quartztop.bonus.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    int id;
    String email;
    String fio;
    String phone;
    String manager;
    String nameSalon;
    String city;
    String address;
    String token;
    String createDate;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Позволяет использовать поле только для записи (при десериализации)
    String password;

    @Override
    public String toString() {
        return "User " + fio + " email " + email +
                " phone " + phone + " manager " + manager + " salom " + nameSalon +
                " adress " + address;
    }
}
