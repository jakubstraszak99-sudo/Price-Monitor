package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.PriceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistoryEntity, Long> {

    List<PriceHistoryEntity> findAllByProductIdOrderByTimestampDesc(final Long productId);

}