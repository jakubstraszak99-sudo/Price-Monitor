package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.page.PriceAlertPage;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface PriceAlertService {

    void createPriceAlert(final String url,
                          final BigDecimal targetPrice,
                          final ScrapedProduct scrapedProduct,
                          final UUID userPublicId);

    PriceAlertPage getPriceAlerts(final Pageable pageable,
                                  final String search,
                                  final UUID userPublicId);

}
