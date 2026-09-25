package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.PriceAlert;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.page.PriceAlertPage;
import com.github.pricemonitor.model.request.alert.UpdatePriceAlertRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface PriceAlertService {

    void createPriceAlert(final String url,
                          final BigDecimal targetPrice,
                          final ScrapedProduct scrapedProduct,
                          final UUID userPublicId);

    boolean checkAlertExists(final UUID userPublicId, final String productUrl);

    PriceAlertPage getPriceAlerts(final Pageable pageable,
                                  final String search,
                                  final UUID userPublicId);

    PriceAlert updatePriceAlert(final UUID alertPublicId, final UpdatePriceAlertRequest request, final UUID userPublicId);

    void deletePriceAlert(final UUID alertPublicId, final UUID userPublicId);

}
