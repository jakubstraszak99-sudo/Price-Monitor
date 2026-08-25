package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.PriceAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlertEntity, Long> {

    List<PriceAlertEntity> findAllByUserId(final Long userId);

    List<PriceAlertEntity> findAllByProductIdAndActiveTrue(final Long productId);

}
