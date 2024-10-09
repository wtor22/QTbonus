package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity,Integer> {

    UserEntity findByEmail(String email);


    UserEntity findByFio(String fio);
    @Query("Select u From UserEntity u WHERE u.roles = ?1 ORDER BY id")
    List<UserEntity> findByRoles(Roles roles);
}
