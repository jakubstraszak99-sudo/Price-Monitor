package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistory(
        Long id,
        BigDecimal recordedPrice,
        LocalDateTime timestamp
) {}