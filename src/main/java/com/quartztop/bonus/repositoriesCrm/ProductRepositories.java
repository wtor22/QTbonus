package com.quartztop.bonus.repositoriesCrm;

import com.quartztop.bonus.crm.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepositories extends JpaRepository<Product, Integer> {

    List<Product> findByDescription(String name);

    Product findByExternalId(String externalId);

    List<Product> findByDescriptionContainingIgnoreCaseAndTypeProduct(String name,String typeProduct);
}
