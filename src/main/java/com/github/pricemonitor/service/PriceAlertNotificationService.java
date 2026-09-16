package com.github.pricemonitor.service;

import com.github.pricemonitor.model.entity.ProductEntity;

import java.math.BigDecimal;

public interface PriceAlertNotificationService {

    void notifyAboutPriceChange(final ProductEntity product, final BigDecimal newPrice);

}
