package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.TypeActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeActivityRepositories extends JpaRepository<TypeActivity, Integer> {
    
    TypeActivity findByName(String name);

}
