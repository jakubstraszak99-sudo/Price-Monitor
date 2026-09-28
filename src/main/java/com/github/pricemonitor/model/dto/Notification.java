package com.github.pricemonitor.model.dto;

import com.github.pricemonitor.model.helper.NotificationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Notification(
        UUID publicId,
        NotificationType type,
        Product product,
        String productName,
        Boolean read,
        BigDecimal triggerPrice,
        Instant createdAt
) {}
