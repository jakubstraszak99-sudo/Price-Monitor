package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceHistory(
        BigDecimal recordedPrice,
        Instant createdAt
) {}