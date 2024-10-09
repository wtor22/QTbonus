package com.quartztop.bonus.repositoriesCrm;

import com.quartztop.bonus.crm.Invoices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoicesRepository extends JpaRepository<Invoices, Integer> {

    List<Invoices> findByNameAndMomentBetween(String name, LocalDateTime momentStartDay,
                                                      LocalDateTime momentEndDay);


    List<Invoices> findByNameAndAgentInnAndMomentBetween(String name, String inn,
                                                         LocalDateTime momentStartDay,
                                                         LocalDateTime momentEndDay);

    @Query("SELECT i FROM Invoices i WHERE i.agent.id IN :agentsId AND i.moment BETWEEN :momentStartDay AND :momentEndDay")
    List<Invoices> findByAgentsIdAndMomentBetween(@Param("agentsId") List<Integer> agentsId,
                                                 LocalDateTime momentStartDay,
                                                 LocalDateTime momentEndDay);

    Optional<Invoices> findByNameAndExternalId(String name, String externalId);
}
