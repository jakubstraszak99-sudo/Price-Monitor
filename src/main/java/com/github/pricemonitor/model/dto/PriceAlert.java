package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;

public record PriceAlert(
        Long id,
        BigDecimal targetPrice,
        Boolean active,
        Long productId
) {}