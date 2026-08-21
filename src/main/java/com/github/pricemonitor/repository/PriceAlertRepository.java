package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert> findAllByUserId(final Long userId);

    List<PriceAlert> findAllByProductIdAndActiveTrue(final Long productId);

}
