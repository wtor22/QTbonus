package com.quartztop.bonus.repositoriesCrm;

import com.quartztop.bonus.crm.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Integer> {

    Optional<Agent> findByInn(String inn);

    @Query("SELECT a.Id FROM Agent a WHERE a.fullName = :agentFullName")
    List<Integer> findAgentIdsByFullName(@Param("agentFullName") String agentFullName);

    List<Agent> findAllByName(String agentName);


}
