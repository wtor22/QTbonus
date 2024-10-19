package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.user.UserEntity;
import com.quartztop.bonus.user.roles.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Integer> {

    //UserEntity findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    Optional<UserEntity> findByEmail (String email);

    @Query("SELECT u FROM UserEntity u WHERE u.createDate IS NOT NULL")
    List<UserEntity> findAllWithCreateDate();

    @Query("SELECT u FROM UserEntity u WHERE u.createDate IS NOT NULL AND u.manager = ?1")
    List<UserEntity> findAllWithCreateDateAndManager(UserEntity manager);


    UserEntity findByFio(String fio);
    @Query("Select u From UserEntity u WHERE u.roles = ?1 ORDER BY id")
    List<UserEntity> findByRoles(Roles roles);
}
