package com.quartztop.bonus.repositoriesCrm;

import com.quartztop.bonus.crm.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Integer> {

    Optional<Agent> findByInn(String inn);
}
