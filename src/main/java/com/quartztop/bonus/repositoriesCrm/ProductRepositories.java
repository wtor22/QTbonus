package com.quartztop.bonus.repositoriesCrm;

import com.quartztop.bonus.crm.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepositories extends JpaRepository<Product, Integer> {

    List<Product> findByDescription(String name);

    Product findByExternalId(String externalId);

    List<Product> findByDescriptionContainingIgnoreCaseAndTypeProduct(String name,String typeProduct);

    // Поиск товаров по описанию или артикулу (независимо от регистра)
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.article) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND p.typeProduct = :typeProduct")
    List<Product> searchByDescriptionOrArticleAndType(@Param("query") String query,
                                                      @Param("typeProduct") String typeProduct);
}
