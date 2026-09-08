package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PriceAlert(
        UUID publicId,
        BigDecimal targetPrice,
        Boolean active,
        Product product,
        LocalDateTime createdAt
) {}