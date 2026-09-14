package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.model.dto.PriceHistory;
import com.github.pricemonitor.model.entity.PriceHistoryEntity;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.mapper.PriceHistoryMapper;
import com.github.pricemonitor.repository.PriceHistoryRepository;
import com.github.pricemonitor.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceHistoryMapper priceHistoryMapper;

    @Override
    @Transactional
    public void createPriceHistory(final ProductEntity product) {
        this.savePriceHistory(product, product.getCurrentPrice());
    }

    @Override
    @Transactional
    public void createPriceHistory(final ProductEntity product, final BigDecimal price) {
        this.savePriceHistory(product, price);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceHistory> getPriceHistory(final String productUrl) {
        return this.priceHistoryRepository.findByProductProductUrl(productUrl).stream()
                .map(this.priceHistoryMapper::map)
                .sorted(Comparator.comparing(PriceHistory::createdAt))
                .toList();
    }

    private void savePriceHistory(final ProductEntity product, final BigDecimal price) {
        this.priceHistoryRepository.save(PriceHistoryEntity.builder()
                .recordedPrice(price)
                .product(product)
                .build());
    }

}
