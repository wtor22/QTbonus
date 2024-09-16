package com.quartztop.bonus.repositoriesCrm;

import com.quartztop.bonus.crm.InvoicePosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionInvoiceRepository extends JpaRepository<InvoicePosition, Integer> {

    List<InvoicePosition> findAllByExternalInvoiceId(String externalInvoiceId);
}
