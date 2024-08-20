package com.quartztop.bonus.user.roles;

import com.quartztop.bonus.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Integer> {

    Roles findByRole(String role);

    @Query("SELECT r FROM Roles r WHERE r.role <> :role")
    List<Roles> findAllExceptRole(@Param("role") String role);
}
