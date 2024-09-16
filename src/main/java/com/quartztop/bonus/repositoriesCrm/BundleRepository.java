package com.quartztop.bonus.repositoriesCrm;

import com.quartztop.bonus.crm.Bundle;
import com.quartztop.bonus.crm.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BundleRepository extends JpaRepository<Bundle, Integer> {

    Bundle findByExternalId(String externalId);
    Bundle findByProduct(Product product);
}
