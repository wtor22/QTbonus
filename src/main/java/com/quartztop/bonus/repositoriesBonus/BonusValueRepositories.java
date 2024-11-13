package com.quartztop.bonus.repositoriesBonus;

import com.quartztop.bonus.orders.BonusValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BonusValueRepositories extends JpaRepository<BonusValue,Integer> {

    public BonusValue findByName(String name);
}
