package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record ScrapedProduct(
        String name,
        BigDecimal price,
        String imageUrl,
        Currency currency
) {}
