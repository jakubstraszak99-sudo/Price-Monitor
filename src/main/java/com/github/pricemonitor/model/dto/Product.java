package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Currency;

public record Product(
        Long id,
        String name,
        URI productUrl,
        URI imageUrl,
        Currency currency,
        BigDecimal currentPrice,
        LocalDateTime lastUpdated
) {}
