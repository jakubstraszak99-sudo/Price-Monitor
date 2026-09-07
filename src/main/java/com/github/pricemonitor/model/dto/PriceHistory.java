package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistory(
        BigDecimal recordedPrice,
        LocalDateTime timestamp
) {}