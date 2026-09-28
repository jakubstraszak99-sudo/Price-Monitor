package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Currency;

public record Product(
        String name,
        URI productUrl,
        URI imageUrl,
        Currency currency,
        BigDecimal currentPrice,
        Instant lastUpdated,
        String shop,
        URI faviconUrl,
        Boolean available
) {}
