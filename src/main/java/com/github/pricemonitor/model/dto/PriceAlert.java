package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PriceAlert(
        UUID publicId,
        BigDecimal targetPrice,
        Boolean active,
        Product product,
        Instant createdAt
) {}