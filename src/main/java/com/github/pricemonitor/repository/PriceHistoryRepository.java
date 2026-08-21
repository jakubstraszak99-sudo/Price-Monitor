package com.github.pricemonitor.repository;

import com.github.pricemonitor.model.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findAllByProductIdOrderByTimestampDesc(final Long productId);

}