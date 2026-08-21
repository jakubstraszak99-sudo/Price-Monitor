package com.github.pricemonitor.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceAlertDto {

    private Long id;
    private BigDecimal targetPrice;
    private Boolean isActive;
    private Long userId;
    private Long productId;

}