package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.tokens.TokenEntity;
import com.quartztop.bonus.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenEntity,Integer> {

    Optional<TokenEntity> findByToken(String token);

    // Метод для поиска токена с самой поздней датой истечения для конкретного менеджера
    @Query("SELECT t FROM TokenEntity t WHERE t.managerId = :managerId ORDER BY t.expiryDate DESC")
    List<TokenEntity> findLatestTokenByManagerId(@Param("managerId") UserEntity manager);
}
