package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.PriceAlertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlertEntity, Long> {

    boolean existsByUserPublicIdAndProductProductUrl(final UUID userPublicId, final String productUrl);

    @EntityGraph(attributePaths = "product")
    Page<PriceAlertEntity> findByUserPublicId(final UUID userPublicId, final Pageable pageable);

    @EntityGraph(attributePaths = "product")
    Page<PriceAlertEntity> findByUserPublicIdAndProductNameContainingIgnoreCase(final UUID userPublicId, final String search, final Pageable pageable);

    Optional<PriceAlertEntity> findByPublicId(final UUID publicId);

    void deleteByPublicId(final UUID publicId);

    @Query("SELECT a FROM PriceAlertEntity a JOIN FETCH a.user WHERE a.product.id = :productId AND a.active = true AND a.targetPrice >= :newPrice")
    List<PriceAlertEntity> findActiveAlertsForProduct(final Long productId, final BigDecimal newPrice);

}
