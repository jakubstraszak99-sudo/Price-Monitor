package com.github.pricemonitor.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;

@Data
public class ProductDto {

    private Long id;
    private String name;
    private URI url;
    private BigDecimal currentPrice;
    private LocalDateTime lastUpdated;

}
