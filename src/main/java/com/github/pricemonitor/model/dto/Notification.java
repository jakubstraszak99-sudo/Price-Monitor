package com.github.pricemonitor.model.dto;

import com.github.pricemonitor.model.helper.NotificationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Notification(
        UUID publicId,
        NotificationType type,
        Product product,
        boolean read,
        BigDecimal triggerPrice,
        LocalDateTime createdAt
) {}
