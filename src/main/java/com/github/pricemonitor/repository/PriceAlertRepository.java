package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.PriceAlertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlertEntity, Long> {

    boolean existsByUserPublicIdAndProductProductUrl(final UUID userPublicId, final String productUrl);

    Page<PriceAlertEntity> findByUserPublicId(final UUID userPublicId, final Pageable pageable);

    Page<PriceAlertEntity> findByUserPublicIdAndProductNameContainingIgnoreCase(final UUID userPublicId, final String search, final Pageable pageable);

    Optional<PriceAlertEntity> findByPublicId(final UUID publicId);

    void deleteByPublicId(final UUID publicId);

}
