package com.quartztop.bonus.user;

import com.quartztop.bonus.user.roles.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Integer> {
    UserEntity findByEmail(String email);



    @Query("Select u From UserEntity u WHERE u.roles = ?1 ORDER BY id")
    List<UserEntity> findByRoles(Integer roles);
}
