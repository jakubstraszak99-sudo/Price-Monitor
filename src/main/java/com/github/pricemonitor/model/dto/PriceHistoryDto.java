package com.github.pricemonitor.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceHistoryDto {

    private Long id;
    private BigDecimal recordedPrice;
    private LocalDateTime timestamp;
    private Long productId;

}