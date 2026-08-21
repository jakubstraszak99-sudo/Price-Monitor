package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;

public record PriceAlertDto (
        Long id,
        BigDecimal targetPrice,
        Boolean active,
        Long userId,
        Long productId
) {}