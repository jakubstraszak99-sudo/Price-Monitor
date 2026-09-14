package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.PriceHistory;
import com.github.pricemonitor.model.entity.ProductEntity;

import java.math.BigDecimal;
import java.util.List;

public interface PriceHistoryService {

    void createPriceHistory(final ProductEntity product);

    void createPriceHistory(final ProductEntity product, final BigDecimal price);

    List<PriceHistory> getPriceHistory(final String productUrl);

}
