package com.github.pricemonitor.model.request.alert;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePriceAlertRequest(
        @NotBlank
        String url,

        @NotNull
        @Positive
        BigDecimal targetPrice,

        @NotNull
        ScrapedProduct scrapedProduct
) {}
