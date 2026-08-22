package com.github.pricemonitor.kafka.event;

public record EmailNotificationEvent(
        String email,
        String token
) {}
