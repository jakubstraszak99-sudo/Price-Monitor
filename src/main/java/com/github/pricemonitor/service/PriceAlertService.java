package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.ScrapedProduct;

import java.math.BigDecimal;
import java.util.UUID;

public interface PriceAlertService {

    void createPriceAlert(final String url,
                          final BigDecimal targetPrice,
                          final ScrapedProduct scrapedProduct,
                          final UUID userPublicId);

}
