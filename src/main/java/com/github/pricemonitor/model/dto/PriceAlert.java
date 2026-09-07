package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;

public record PriceAlert(
        BigDecimal targetPrice,
        Boolean active,
        Long productId
) {}