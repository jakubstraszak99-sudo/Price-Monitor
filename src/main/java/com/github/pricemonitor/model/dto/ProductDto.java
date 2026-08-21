package com.github.pricemonitor.model.dto;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;

public record ProductDto (
        Long id,
        String name,
        URI url,
        BigDecimal currentPrice,
        LocalDateTime lastUpdated
){}
