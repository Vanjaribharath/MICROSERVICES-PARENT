package com.capgemini.training.systemapi.adapter.out.persistence;

import com.capgemini.training.systemapi.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Day 8 — Spring Data JPA Repository.
 * FIX: findByCategory_Id uses Spring Data property traversal (category.id).
 * findByCategoryId would look for a field named 'categoryId' — entity has 'category' object.
 */
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    Page<ProductJpaEntity> findByStatus(ProductStatus status, Pageable pageable);

    // FIX: underscore tells Spring Data to traverse: product.category.id
    List<ProductJpaEntity> findByCategory_Id(Long categoryId);

    @Query("SELECT p FROM ProductJpaEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<ProductJpaEntity> search(@Param("q") String query, Pageable pageable);
}
