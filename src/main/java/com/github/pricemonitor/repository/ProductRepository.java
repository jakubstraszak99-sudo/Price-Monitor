package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByProductUrl(final String url);

    @Query("SELECT p FROM ProductEntity p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.shop) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ProductEntity> searchByNameOrShop(final String search, final Pageable pageable);

}
