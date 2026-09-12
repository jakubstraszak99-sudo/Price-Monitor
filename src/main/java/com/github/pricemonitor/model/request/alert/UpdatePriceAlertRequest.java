package com.github.pricemonitor.model.request.alert;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdatePriceAlertRequest(
        Boolean active,

        @Positive
        BigDecimal targetPrice
) {}