package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistoryDto(
        Long id,
        BigDecimal recordedPrice,
        LocalDateTime timestamp,
        Long productId
) {}