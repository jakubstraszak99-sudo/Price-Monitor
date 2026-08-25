package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;

public record ScrapedProduct(
        String name,
        BigDecimal price,
        URI imageUrl,
        Currency currency
) {}
