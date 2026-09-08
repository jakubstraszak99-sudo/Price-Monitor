package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.PriceAlertEntity;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlertEntity, Long> {

    boolean existsByUserAndProduct(final UserEntity user, final ProductEntity product);

    Page<PriceAlertEntity> findByUser(final UserEntity user, final Pageable pageable);

    Page<PriceAlertEntity> findByUserAndProductNameContainingIgnoreCase(final UserEntity user, final String search, final Pageable pageable);

}
